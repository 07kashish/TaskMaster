# TaskMaster Backend

Production-ready REST API for Task Tracking and Team Collaboration.

## Tech Stack

- Java 17
- Spring Boot 3
- Gradle
- MySQL 8
- Spring Security + JWT
- Spring Data JPA (Hibernate)
- Flyway
- Lombok
- springdoc-openapi (Swagger UI)
- JUnit 5 + Testcontainers

## Project Assumptions

- IDs use `Long` auto-increment.
- `POST /api/auth/logout` revokes only the provided refresh token.
- `ROLE_ADMIN` can bypass team membership restrictions.
- Team `OWNER` role changes are restricted by business rules in service layer.
- Attachments are stored on local disk under `uploads/` by default.
- AI endpoint is stubbed via `AiService` (`AiServiceStubImpl`) for future provider integration.

## Configuration

Default config: `src/main/resources/application.yml`  
Local profile config: `src/main/resources/application-local.yml`

Example local DB values:

- DB: `taskmaster`
- User: `taskmaster_user`
- Password: `taskmaster_pass`

## Run

From project root:

```bash
./gradlew bootRun --args="--spring.profiles.active=local --server.port=9090"
```

Windows PowerShell:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local --server.port=9090"
```

## Verify App

- Health: `http://localhost:9090/health`
- Swagger UI: `http://localhost:9090/swagger-ui.html`
- OpenAPI JSON: `http://localhost:9090/v3/api-docs`

## Authentication Flow (Quick)

1. Register user
2. Login user
3. Copy `accessToken`
4. Use header `Authorization: Bearer <ACCESS_TOKEN>` for protected endpoints

## Key cURL Examples (Port 9090)

Register:

```bash
curl -X POST http://localhost:9090/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","password":"StrongPass123!"}'
```

Login:

```bash
curl -X POST http://localhost:9090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"StrongPass123!"}'
```

Create Team:

```bash
curl -X POST http://localhost:9090/api/teams \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Platform Team","description":"Core platform delivery"}'
```

Invite Member:

```bash
curl -X POST http://localhost:9090/api/teams/<TEAM_ID>/invite \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@example.com"}'
```

Accept Invite:

```bash
curl -X POST "http://localhost:9090/api/teams/invitations/accept?token=<INVITE_TOKEN>" \
  -H "Authorization: Bearer <INVITED_USER_ACCESS_TOKEN>"
```

Create Task:

```bash
curl -X POST http://localhost:9090/api/teams/<TEAM_ID>/tasks \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Build reporting API","description":"Initial endpoint set","priority":"HIGH"}'
```

Assign Task:

```bash
curl -X PATCH http://localhost:9090/api/tasks/<TASK_ID>/assign \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"assigneeUserId":2}'
```

Filter Tasks:

```bash
curl "http://localhost:9090/api/teams/<TEAM_ID>/tasks?status=OPEN&priority=HIGH&q=report&sortBy=dueDate&sortDir=asc&page=0&size=10" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

Add Comment:

```bash
curl -X POST http://localhost:9090/api/tasks/<TASK_ID>/comments \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"content":"Started implementation."}'
```

Upload Attachment:

```bash
curl -X POST http://localhost:9090/api/tasks/<TASK_ID>/attachments \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -F "file=@./spec.pdf"
```
Submission branch update