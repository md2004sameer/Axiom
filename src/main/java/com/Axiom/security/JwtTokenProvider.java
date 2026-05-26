package com.Axiom.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms:3600000}") long validityInMilliseconds) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET configuration is missing or empty. " +
                "Ensure .env file contains JWT_SECRET or set security.jwt.secret property.");
        }
        if (secret.startsWith("default-secret")) {
            throw new IllegalStateException("JWT_SECRET must not use the default development secret");
        }

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        System.out.println("✓ JWT Secret loaded: " + secret.substring(0, Math.min(20, secret.length())) + "... (length: " + secretBytes.length + " bytes)");
        
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                "JWT_SECRET must be at least 32 bytes for HS256 algorithm. " +
                "Current length: " + secretBytes.length + " bytes");
        }

        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.validityInMilliseconds = validityInMilliseconds;
        System.out.println("✓ JwtTokenProvider initialized successfully with " + validityInMilliseconds + "ms expiration");
    }

    public String createToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public Date getExpiration(String token) {
        return parseClaims(token).getBody().getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = parseClaims(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }
}
