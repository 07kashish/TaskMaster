package org.assignment.taskmaster;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TaskMasterApplicationTests {

	@Container
	static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
		.withDatabaseName("taskmaster_test")
		.withUsername("test")
		.withPassword("test");

	@DynamicPropertySource
	static void configure(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
		registry.add("spring.flyway.enabled", () -> true);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void authRegisterLoginAndRefreshRotationWorks() throws Exception {
		JsonNode register = register("alice", "alice@example.com", "StrongPass123!");
		String refresh = register.get("refreshToken").asText();

		JsonNode login = login("alice@example.com", "StrongPass123!");
		String refreshFromLogin = login.get("refreshToken").asText();

		JsonNode refreshed = refresh(refreshFromLogin);
		String rotated = refreshed.get("refreshToken").asText();

		mockMvc.perform(post("/api/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshFromLogin))))
			.andExpect(status().isUnauthorized());

		assert !refresh.equals(refreshFromLogin);
		assert !refreshFromLogin.equals(rotated);
	}

	@Test
	void nonMemberCannotAccessTeamDetails() throws Exception {
		JsonNode ownerAuth = register("owner", "owner@example.com", "StrongPass123!");
		JsonNode outsiderAuth = register("outsider", "outsider@example.com", "StrongPass123!");

		long teamId = createTeam(ownerAuth.get("accessToken").asText(), "Core Team");

		mockMvc.perform(get("/api/teams/{teamId}", teamId)
				.header("Authorization", "Bearer " + outsiderAuth.get("accessToken").asText()))
			.andExpect(status().isForbidden());
	}

	@Test
	void memberCanCreateTaskAfterInviteAcceptance() throws Exception {
		JsonNode ownerAuth = register("owner2", "owner2@example.com", "StrongPass123!");
		JsonNode memberAuth = register("member2", "member2@example.com", "StrongPass123!");
		String ownerToken = ownerAuth.get("accessToken").asText();
		String memberToken = memberAuth.get("accessToken").asText();

		long teamId = createTeam(ownerToken, "Delivery Team");
		String inviteToken = invite(ownerToken, teamId, "member2@example.com");
		acceptInvite(memberToken, inviteToken);

		mockMvc.perform(post("/api/teams/{teamId}/tasks", teamId)
				.header("Authorization", "Bearer " + memberToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"title", "Implement API",
					"description", "Create task endpoint",
					"priority", "HIGH"
				))))
			.andExpect(status().isCreated());
	}

	@Test
	void teamTaskFilteringByStatusWorks() throws Exception {
		JsonNode ownerAuth = register("owner3", "owner3@example.com", "StrongPass123!");
		String token = ownerAuth.get("accessToken").asText();
		long teamId = createTeam(token, "Ops Team");
		long taskId = createTask(token, teamId, "Close release");

		mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("status", "COMPLETED"))))
			.andExpect(status().isOk());

		MvcResult result = mockMvc.perform(get("/api/teams/{teamId}/tasks", teamId)
				.header("Authorization", "Bearer " + token)
				.queryParam("status", "COMPLETED"))
			.andExpect(status().isOk())
			.andReturn();

		JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
		assert body.get("totalElements").asInt() == 1;
	}

	@Test
	void onlyManagerCanAssignTasks() throws Exception {
		JsonNode ownerAuth = register("owner4", "owner4@example.com", "StrongPass123!");
		JsonNode memberAuth = register("member4", "member4@example.com", "StrongPass123!");
		String ownerToken = ownerAuth.get("accessToken").asText();
		String memberToken = memberAuth.get("accessToken").asText();
		long memberId = memberAuth.get("user").get("id").asLong();

		long teamId = createTeam(ownerToken, "QA Team");
		String inviteToken = invite(ownerToken, teamId, "member4@example.com");
		acceptInvite(memberToken, inviteToken);
		long taskId = createTask(ownerToken, teamId, "Run regression");

		mockMvc.perform(patch("/api/tasks/{taskId}/assign", taskId)
				.header("Authorization", "Bearer " + memberToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("assigneeUserId", memberId))))
			.andExpect(status().isForbidden());

		MvcResult assigned = mockMvc.perform(patch("/api/tasks/{taskId}/assign", taskId)
				.header("Authorization", "Bearer " + ownerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("assigneeUserId", memberId))))
			.andExpect(status().isOk())
			.andReturn();

		JsonNode body = objectMapper.readTree(assigned.getResponse().getContentAsString());
		assert body.get("assignedTo").asLong() == memberId;
	}

	private JsonNode register(String name, String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"name", name,
					"email", email,
					"password", password
				))))
			.andExpect(status().isCreated())
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString());
	}

	private JsonNode login(String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"email", email,
					"password", password
				))))
			.andExpect(status().isOk())
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString());
	}

	private JsonNode refresh(String refreshToken) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
			.andExpect(status().isOk())
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString());
	}

	private long createTeam(String accessToken, String name) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/teams")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("name", name, "description", "desc"))))
			.andExpect(status().isCreated())
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}

	private String invite(String accessToken, long teamId, String email) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/teams/{teamId}/invite", teamId)
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("email", email))))
			.andExpect(status().isCreated())
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
	}

	private void acceptInvite(String accessToken, String token) throws Exception {
		mockMvc.perform(post("/api/teams/invitations/accept")
				.header("Authorization", "Bearer " + accessToken)
				.queryParam("token", token))
			.andExpect(status().isOk());
	}

	private long createTask(String accessToken, long teamId, String title) throws Exception {
		Map<String, Object> payload = new HashMap<>();
		payload.put("title", title);
		payload.put("description", "desc");
		payload.put("priority", "MEDIUM");

		MvcResult result = mockMvc.perform(post("/api/teams/{teamId}/tasks", teamId)
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(payload)))
			.andExpect(status().isCreated())
			.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}

}
