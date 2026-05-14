package com.apimonitor.modules.monitoring.dto;


public record StatusCodeDistributionDto(
        int  statusCode,
        long count,
        String category
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