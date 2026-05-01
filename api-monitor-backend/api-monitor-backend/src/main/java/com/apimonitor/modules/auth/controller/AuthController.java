package com.apimonitor.modules.auth.controller;

import com.apimonitor.common.exception.ApiException;
import com.apimonitor.common.response.ApiResponse;
import com.apimonitor.modules.auth.dto.AuthResponse;
import com.apimonitor.modules.auth.dto.LoginRequest;
import com.apimonitor.modules.auth.dto.RegisterRequest;
import com.apimonitor.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints — all PUBLIC (no JWT required).
 *
 * POST /api/v1/auth/register  → create account, returns tokens
 * POST /api/v1/auth/login     → authenticate, returns tokens
 * POST /api/v1/auth/refresh   → exchange refresh token for new access token
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse tokens = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", tokens));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse tokens = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", tokens));
    }

    /**
     * Refresh token is passed in Authorization header: "Bearer <refresh_token>"
     */
    // AuthController.java — replace refresh method body
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException(
                    "Authorization header must be: Bearer <refresh_token>",
                    HttpStatus.UNAUTHORIZED);
        }
        String refreshToken = authHeader.substring(7);
        AuthResponse tokens = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", tokens));
    }

    private String extractBearer(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}