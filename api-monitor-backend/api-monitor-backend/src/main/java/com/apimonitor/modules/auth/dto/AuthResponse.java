package com.apimonitor.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body for login, register, and refresh endpoints.
 *
 * Java 21 record with custom JSON property names.
 * access_token / refresh_token use snake_case per OAuth2 convention.
 */
public record AuthResponse(

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        Long userId,
        String username,
        String email,
        String role
) {}