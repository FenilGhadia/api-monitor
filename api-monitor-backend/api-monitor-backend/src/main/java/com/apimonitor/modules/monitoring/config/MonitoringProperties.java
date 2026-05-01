package com.apimonitor.modules.monitoring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strongly-typed binding for the application.monitoring config block.
 *
 * Centralizes all tunable monitoring parameters so they can be
 * adjusted in application.yml without touching Java code.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "application.monitoring")
public class MonitoringProperties {

    /**
     * URL path prefixes that should NOT be logged.
     * Defaults to auth, logs, analytics, actuator, error.
     */
    private List<String> excludedPaths = List.of(
            "/api/v1/auth",
            "/api/v1/logs",
            "/api/v1/analytics",
            "/actuator",
            "/error"
    );

    /**
     * Requests slower than this threshold are flagged as slow (ms).
     */
    private long slowRequestThresholdMs = 1000L;

    /**
     * Alert thresholds for the analytics service.
     */
    private Alert alert = new Alert();

    @Getter @Setter
    public static class Alert {
        /** Error rate % above which an alert is triggered */
        private double errorRateThreshold = 10.0;

        /** Average response time (ms) above which an alert is triggered */
        private long responseTimeThresholdMs = 2000L;
    }
}