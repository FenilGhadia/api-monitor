package com.apimonitor.modules.monitoring.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


public record TimeSeriesDto(
        Instant bucket,
        long    requestCount,
        double  avgResponseMs,
        long    errorCount
) {

    public static TimeSeriesDto fromRow(Object[] row) {

        LocalDateTime ldt = switch (row[0]) {
            case LocalDateTime l        -> l;
            case java.sql.Timestamp ts  -> ts.toLocalDateTime();
            default -> throw new IllegalArgumentException(
                    "Unexpected bucket type from DB: " + row[0].getClass().getName());
        };

        Instant bucket  = ldt.toInstant(ZoneOffset.UTC);
        long   requests = ((Number) row[1]).longValue();
        double avgTime  = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
        long   errors   = ((Number) row[3]).longValue();

        return new TimeSeriesDto(bucket, requests, avgTime, errors);
    }
}