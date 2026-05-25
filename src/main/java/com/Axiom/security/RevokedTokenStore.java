package com.Axiom.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RevokedTokenStore {

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String token, Instant expiresAt) {
        removeExpiredTokens();
        if (expiresAt.isAfter(Instant.now())) {
            revokedTokens.put(hash(token), expiresAt);
        }
    }

    public boolean isRevoked(String token) {
        removeExpiredTokens();
        return revokedTokens.containsKey(hash(token));
    }

    private void removeExpiredTokens() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException cause) {
            throw new IllegalStateException("SHA-256 is not available", cause);
        }
    }
}
