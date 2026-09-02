package com.nextpick.backend.security;

import com.nextpick.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {
    private final JwtProperties properties;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
    }

    private SecretKey key() {
        byte[] decoded = Decoders.BASE64.decode(properties.secret());
        return Keys.hmacShaKeyFor(decoded);
    }

    public String generateAccessToken(String email, Map<String, Object> extraClaims) {
        return buildToken(email, merge(extraClaims, Map.of("type", "access")), properties.accessExpirationMs());
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, Map.of("type", "refresh", "jti", UUID.randomUUID().toString()), properties.refreshExpirationMs());
    }

    private String buildToken(String subject, Map<String, Object> claims, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, String expectedEmail) {
        try {
            final String email = extractEmail(token);
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return email.equals(expectedEmail) && "access".equals(type) && !isExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /** Comprueba que el token no ha expirado y que efectivamente es de tipo "refresh". */
    public boolean isRefreshTokenValid(String token) {
        try {
            String type = (String) extractClaim(token, claims -> claims.get("type"));
            return "refresh".equals(type) && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload();
        return resolver.apply(claims);
    }

    public long refreshExpirationMs() {
        return properties.refreshExpirationMs();
    }

    private Map<String, Object> merge(Map<String, Object> first, Map<String, Object> second) {
        java.util.HashMap<String, Object> merged = new java.util.HashMap<>(first);
        merged.putAll(second);
        return merged;
    }
}
