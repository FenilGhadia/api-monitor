package com.apimonitor.modules.monitoring.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Query parameters for GET /api/v1/logs
 *
 * All fields are optional — null means "no filter".
 * Spring binds these from request parameters automatically.
 *
 * Example:
 *   GET /api/v1/logs?endpoint=/api/v1/products&httpMethod=GET&errorOnly=true&page=0&size=20
 */
public record ApiLogFilterRequest(
        String endpoint,
        String httpMethod,
        Integer statusCode,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime fromDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime toDate,

        Long    userId,
        Boolean slowOnly,
        Boolean errorOnly
) {
    /** Canonical default constructor — all filters off */
    public ApiLogFilterRequest() {
        this(null, null, null, null, null, null, false, false);
    }
}