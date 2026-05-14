package com.apimonitor.modules.monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;


public record ExternalLogRequest(

        @NotBlank(message = "serviceName is required")
        String serviceName,

        @NotBlank(message = "endpoint is required")
        String endpoint,

        @NotBlank(message = "httpMethod is required")
        String httpMethod,

        @NotNull(message = "statusCode is required")
        Integer statusCode,

        @NotNull(message = "responseTimeMs is required")
        @Positive(message = "responseTimeMs must be positive")
        Long responseTimeMs,


        LocalDateTime timestamp
) {}