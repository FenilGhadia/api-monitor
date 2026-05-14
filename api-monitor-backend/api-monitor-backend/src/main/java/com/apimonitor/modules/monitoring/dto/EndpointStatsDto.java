package com.apimonitor.modules.monitoring.dto;


public record EndpointStatsDto(
        String endpoint,
        long   totalRequests,
        double avgResponseTimeMs,
        long   errorCount,
        long   slowCount,
        double errorRate
) {

    public static EndpointStatsDto fromRow(Object[] row) {
        String endpoint  = (String) row[0];
        long   total     = ((Number) row[1]).longValue();

        double avgTime   = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
        long   errors    = ((Number) row[3]).longValue();
        long   slow      = ((Number) row[4]).longValue();
        double errorRate = total > 0 ? ((double) errors / total) * 100.0 : 0.0;

        return new EndpointStatsDto(endpoint, total, avgTime, errors, slow, errorRate);
    }
}