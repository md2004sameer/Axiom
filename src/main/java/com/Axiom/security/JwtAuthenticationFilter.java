package com.Axiom.security;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// Change 4: reads ax_tok from cookies instead of Authorization header
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    static final String COOKIE_NAME = "ax_tok";

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final RevokedTokenStore revokedTokenStore;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   UserDetailsService userDetailsService,
                                   RevokedTokenStore revokedTokenStore) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.revokedTokenStore = revokedTokenStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveTokenFromCookie(request);

        if (token != null && tokenProvider.validateToken(token) && !revokedTokenStore.isRevoked(token)) {
            String username = tokenProvider.getUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
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