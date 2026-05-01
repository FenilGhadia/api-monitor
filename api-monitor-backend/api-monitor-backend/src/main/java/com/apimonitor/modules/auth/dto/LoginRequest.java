package com.apimonitor.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/v1/auth/login
 * Java 21 record — immutable, compact, no boilerplate.
 */
public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}