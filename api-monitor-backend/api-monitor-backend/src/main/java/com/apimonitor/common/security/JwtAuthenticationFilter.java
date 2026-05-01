package com.apimonitor.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — lives in common/security.
 * Runs once per request, validates the Bearer token, and sets
 * the SecurityContext so downstream filters see an authenticated user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER      = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTH_HEADER);

        // No Bearer token → skip authentication (Spring Security handles 401)
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            chain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER.length());

        try {
            final String userEmail = jwtService.extractUsername(jwt);

            // Only authenticate if not already set in this request's context
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authenticated [{}] → {}", userEmail, request.getRequestURI());
                }
            }
        } catch (Exception e) {
            log.warn("JWT auth failed for [{}]: {}", request.getRequestURI(), e.getMessage());
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip for public auth endpoints — minor optimization
        return request.getServletPath().startsWith("/api/v1/auth/");
    }
}