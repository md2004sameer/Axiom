package com.Axiom.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    static final String COOKIE_NAME = "ax_tok";

    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RevokedTokenStore revokedTokenStore;
    private final long cookieMaxAgeSeconds;

    public AuthController(CustomUserDetailsService userDetailsService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          RevokedTokenStore revokedTokenStore,
                          @Value("${security.jwt.expiration-ms:3600000}") long expirationMs) {
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.revokedTokenStore = revokedTokenStore;
        this.cookieMaxAgeSeconds = expirationMs / 1000;
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

    // Change 1: set httpOnly; Secure; SameSite=Strict cookie, return {username} in body
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = tokenProvider.createToken(request.getUsername());

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(cookieMaxAgeSeconds)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                        "message", "Login successful",
                        "username", request.getUsername()
                ));
    }

    // Change 2: logout clears the cookie with maxAge=0; reads token from cookie
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = resolveTokenFromCookie(request);
        if (token == null || !tokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Valid session cookie is required");
        }

        revokedTokenStore.revoke(token, tokenProvider.getExpiration(token).toInstant());

        ResponseCookie clearCookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(Map.of("message", "Logout successful"));
    }

    // Change 3: GET /api/auth/me — returns the currently authenticated user's username
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return ResponseEntity.ok(Map.of("username", userDetails.getUsername()));
    }

    private String resolveTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}