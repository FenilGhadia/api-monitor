package com.apimonitor.modules.monitoring.dto;

/**
 * Aggregated summary for the dashboard overview panel.
 *
 * Covers the queried time window (e.g. last 24h).
 *
 * Fields:
 *  totalRequests      — raw count
 *  avgResponseTimeMs  — mean response time
 *  errorRate          — percentage of 4xx/5xx responses (0.0–100.0)
 *  slowRequestCount   — requests exceeding the slow threshold
 *  errorCount         — absolute count of error responses
 *  alertTriggered     — true if any threshold is exceeded
 *  alertMessage       — human-readable alert description (null if no alert)
 */
public record AnalyticsSummaryDto(
        long   totalRequests,
        double avgResponseTimeMs,
        double errorRate,
        long   slowRequestCount,
        long   errorCount,
        boolean alertTriggered,
        String  alertMessage
) {}