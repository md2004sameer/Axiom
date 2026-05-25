package com.Axiom.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class RevokedTokenStoreTest {

    @Test
    void revokedTokenIsReportedRevokedUntilExpiration() {
        RevokedTokenStore store = new RevokedTokenStore();

        store.revoke("token-value", Instant.now().plusSeconds(60));

        assertTrue(store.isRevoked("token-value"));
        assertFalse(store.isRevoked("other-token"));
    }

    @Test
    void expiredRevocationIsNotRetained() {
        RevokedTokenStore store = new RevokedTokenStore();

        store.revoke("token-value", Instant.now().minusSeconds(1));

        assertFalse(store.isRevoked("token-value"));
    }
}
