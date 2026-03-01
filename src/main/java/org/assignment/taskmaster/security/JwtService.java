package org.assignment.taskmaster.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey accessKey;
    private final long accessExpirySeconds;

    public JwtService(@Value("${app.jwt.access-secret}") String accessSecret,
                      @Value("${app.jwt.access-expiry-seconds}") long accessExpirySeconds) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.accessExpirySeconds = accessExpirySeconds;
    }

    public String generateAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("email", email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(accessExpirySeconds)))
            .signWith(accessKey)
            .compact();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getAccessExpirySeconds() {
        return accessExpirySeconds;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(accessKey).build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
