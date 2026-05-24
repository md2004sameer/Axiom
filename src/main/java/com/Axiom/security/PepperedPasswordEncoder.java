package com.Axiom.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PepperedPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder delegate;

    public PepperedPasswordEncoder() {
        this.delegate = new BCryptPasswordEncoder();
    }

    @Override
    public String encode(CharSequence rawPassword) {
        throw new UnsupportedOperationException("Use encode(rawPassword, pepper) for per-user peppered passwords");
    }

    public String encode(CharSequence rawPassword, String pepper) {
        return delegate.encode(applyPepper(rawPassword, pepper));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        throw new UnsupportedOperationException("Use matches(rawPassword, encodedPassword, pepper) for per-user peppered passwords");
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword, String pepper) {
        return delegate.matches(applyPepper(rawPassword, pepper), encodedPassword);
    }

    private CharSequence applyPepper(CharSequence rawPassword, String pepper) {
        return rawPassword + pepper;
    }
}
