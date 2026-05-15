package com.boraviajar.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${boraviajar.jwt.secret}") String secret) {
        String trimmed = secret == null ? "" : secret.trim();
        this.key = Keys.hmacShaKeyFor(trimmed.getBytes(StandardCharsets.UTF_8));
    }

    /** Extrai o claim `openId` do JWT (payload igual ao do servidor Node / jose). */
    public Optional<String> parseOpenId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) return Optional.empty();
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object openId = claims.get("openId");
            if (openId instanceof String s && !s.isBlank()) {
                return Optional.of(s);
            }
        } catch (JwtException ignored) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * Emite JWT de sessão compatível com o servidor Node (claims: openId, appId, name, email opcional).
     */
    public String createSessionToken(
            String openId, String name, String email, String appId, long expiresInMs) {
        Instant exp = Instant.now().plusMillis(expiresInMs);
        Map<String, Object> claims = new HashMap<>();
        claims.put("openId", openId);
        claims.put("appId", appId);
        claims.put("name", name != null ? name : "");
        if (email != null && !email.isBlank()) {
            claims.put("email", email);
        }
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .claims(claims)
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }
}
