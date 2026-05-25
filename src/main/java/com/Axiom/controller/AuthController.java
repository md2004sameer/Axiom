package com.Axiom.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.Axiom.auth.LoginRequest;
import com.Axiom.auth.SignupRequest;
import com.Axiom.entity.User;
import com.Axiom.security.JwtTokenProvider;
import com.Axiom.security.RevokedTokenStore;
import com.Axiom.service.CustomUserDetailsService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RevokedTokenStore revokedTokenStore;

    public AuthController(CustomUserDetailsService userDetailsService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          RevokedTokenStore revokedTokenStore) {
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.revokedTokenStore = revokedTokenStore;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequest request) {
        try {
            User user = userDetailsService.registerNewUser(request.getUsername(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User created successfully", "id", user.getId()));
        } catch (IllegalArgumentException cause) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, cause.getMessage(), cause);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = tokenProvider.createToken(request.getUsername());
        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "token", token
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        String token = resolveBearerToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Valid bearer token is required");
        }

        revokedTokenStore.revoke(token, tokenProvider.getExpiration(token).toInstant());
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    private String resolveBearerToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
