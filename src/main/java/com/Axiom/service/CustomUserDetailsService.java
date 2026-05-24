package com.Axiom.service;

import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Axiom.entity.User;
import com.Axiom.repository.UserRepository;
import com.Axiom.security.PepperedPasswordEncoder;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PepperedPasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository, PepperedPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(createAuthority())
        );
    }

    private GrantedAuthority createAuthority() {
        return new SimpleGrantedAuthority("ROLE_USER");
    }

    @Transactional
    public User registerNewUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPepper(generatePepper());
        newUser.setPassword(passwordEncoder.encode(password, newUser.getPepper()));

        return userRepository.save(newUser);
    }

    private String generatePepper() {
        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
