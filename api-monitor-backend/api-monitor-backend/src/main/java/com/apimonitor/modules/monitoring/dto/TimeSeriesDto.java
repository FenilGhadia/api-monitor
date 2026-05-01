package com.apimonitor.modules.monitoring.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * A single bucket in a time-series chart.
 *
 * Returned as a list for both hourly and daily aggregations.
 * The frontend maps this directly to Recharts / Chart.js data arrays.
 *
 * Fix #6: bucket is Instant (not LocalDateTime) so Jackson serialises it with
 * a UTC offset ("2024-01-15T14:00:00Z"), preventing timezone drift on the
 * frontend chart X-axis.
 */
public record TimeSeriesDto(
        Instant bucket,          // Fix #6: was LocalDateTime — now UTC-unambiguous
        long    requestCount,
        double  avgResponseMs,
        long    errorCount
) {
    /**
     * Maps from native query Object[] row.
     * Column order: [bucket(Timestamp|LocalDateTime), request_count, avg_response_ms, error_count]
     *
     * Fix #5: PostgreSQL JDBC 42.3+ returns LocalDateTime, not java.sql.Timestamp.
     *         Pattern matching handles both without ClassCastException.
     */
    public static TimeSeriesDto fromRow(Object[] row) {
        // Fix #5: handle both driver return types defensively
        LocalDateTime ldt = switch (row[0]) {
            case LocalDateTime l        -> l;
            case java.sql.Timestamp ts  -> ts.toLocalDateTime();
            default -> throw new IllegalArgumentException(
                    "Unexpected bucket type from DB: " + row[0].getClass().getName());
        };
        // Fix #6: convert to Instant using UTC (application.yml: jdbc.time_zone=UTC)
        Instant bucket  = ldt.toInstant(ZoneOffset.UTC);
        long   requests = ((Number) row[1]).longValue();
        double avgTime  = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
        long   errors   = ((Number) row[3]).longValue();

        return new TimeSeriesDto(bucket, requests, avgTime, errors);
    }
}