package com.apimonitor.modules.monitoring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "application.monitoring")
public class MonitoringProperties {


    private List<String> excludedPaths = List.of(
            "/api/v1/auth",
            "/api/v1/logs",
            "/api/v1/analytics",
            "/actuator",
            "/error"
    );


    private long slowRequestThresholdMs = 1000L;


    private Alert alert = new Alert();

    @Getter @Setter
    public static class Alert {

        private double errorRateThreshold = 10.0;


        private long responseTimeThresholdMs = 2000L;
    }
}