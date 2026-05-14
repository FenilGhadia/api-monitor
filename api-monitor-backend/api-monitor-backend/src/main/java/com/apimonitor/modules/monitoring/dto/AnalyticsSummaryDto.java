package com.apimonitor.modules.monitoring.dto;


public record AnalyticsSummaryDto(
        long   totalRequests,
        double avgResponseTimeMs,
        double errorRate,
        long   slowRequestCount,
        long   errorCount,
        boolean alertTriggered,
        String  alertMessage
) {}