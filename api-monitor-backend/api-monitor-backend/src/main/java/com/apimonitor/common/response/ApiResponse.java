package com.apimonitor.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;

/**
 * Uniform JSON envelope for every API response.
 *
 * All controllers return ResponseEntity<ApiResponse<T>> so the
 * frontend always receives a predictable shape:
 * {
 *   "success": true,
 *   "message": "...",
 *   "data": { ... },
 *   "timestamp": "2024-01-15T10:00:00Z"
 * }
 *
 * Uses Java 21 — generic type parameter T.
 * @JsonInclude(NON_NULL) suppresses "data":null on void responses.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Instant timestamp;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data    = data;
        this.timestamp = Instant.now();
    }

    // ── Success factories ──

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // ── Failure factories ──

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public static <T> ApiResponse<T> failure(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}