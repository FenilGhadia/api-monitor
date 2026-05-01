package com.apimonitor.modules.monitoring.dto;

import com.apimonitor.modules.monitoring.entity.ApiLog;

import java.time.LocalDateTime;

/**
 * Read-only projection of ApiLog for API responses.
 *
 * Java 21 record — compact, immutable, no-boilerplate DTO.
 */
public record ApiLogDto(
        Long          id,
        String        endpoint,
        String        httpMethod,
        Integer       statusCode,
        Long          responseTimeMs,
        Long          userId,
        String        clientIp,
        boolean       slow,
        boolean       error,
        LocalDateTime timestamp
) {
    /** Maps entity → DTO. Omits userAgent (not exposed in list view). */
    public static ApiLogDto from(ApiLog log) {
        return new ApiLogDto(
                log.getId(),
                log.getEndpoint(),
                log.getHttpMethod(),
                log.getStatusCode(),
                log.getResponseTimeMs(),
                log.getUserId(),
                log.getClientIp(),
                log.isSlow(),
                log.isError(),
                log.getTimestamp()
        );
    }
}