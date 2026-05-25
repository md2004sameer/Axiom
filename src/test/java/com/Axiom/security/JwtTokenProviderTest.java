package com.Axiom.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    @Test
    void rejectsBlankSecret() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new JwtTokenProvider(" ", 3600000));

        assertEquals("JWT_SECRET env var must be configured with a non-blank value", exception.getMessage());
    }

    @Test
    void rejectsDefaultDevelopmentSecret() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new JwtTokenProvider("default-secret-key-for-axiom-project-change-me", 3600000));

        assertEquals("JWT_SECRET env var must not use the default development secret", exception.getMessage());
    }

    @Test
    void rejectsSecretShorterThanHs256Minimum() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new JwtTokenProvider("too-short", 3600000));

        assertEquals("JWT_SECRET env var must be at least 32 bytes for HS256", exception.getMessage());
    }

    @Test
    void createsAndValidatesTokenWithConfiguredSecret() {
        JwtTokenProvider tokenProvider = new JwtTokenProvider("test-jwt-secret-with-at-least-32-bytes", 3600000);

        String token = tokenProvider.createToken("sameer");

        assertTrue(tokenProvider.validateToken(token));
        assertEquals("sameer", tokenProvider.getUsername(token));
    }

    @Test
    void exposesTokenExpirationForRevocationTtl() {
        JwtTokenProvider tokenProvider = new JwtTokenProvider("test-jwt-secret-with-at-least-32-bytes", 3600000);

        String token = tokenProvider.createToken("sameer");

        assertTrue(tokenProvider.getExpiration(token).toInstant().isAfter(Instant.now()));
    }
}
