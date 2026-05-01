package com.apimonitor.modules.monitoring.dto;

/**
 * Status code distribution entry for pie/bar chart on the dashboard.
 *
 * Example dataset:
 * [ {statusCode:200, count:4520}, {statusCode:404, count:31}, {statusCode:500, count:7} ]
 */
public record StatusCodeDistributionDto(
        int  statusCode,
        long count,
        String category   // "2xx" | "3xx" | "4xx" | "5xx"
) {
    public static StatusCodeDistributionDto fromRow(Object[] row) {
        int  code  = ((Number) row[0]).intValue();
        long count = ((Number) row[1]).longValue();
        String cat = switch (code / 100) {
            case 2  -> "2xx";
            case 3  -> "3xx";
            case 4  -> "4xx";
            case 5  -> "5xx";
            default -> "other";
        };
        return new StatusCodeDistributionDto(code, count, cat);
    }
}