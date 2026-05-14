package com.apimonitor.modules.monitoring.dto;

import com.apimonitor.modules.monitoring.entity.ApiLog;

import java.time.LocalDateTime;


public record ApiLogDto(

        Long id,

        String serviceName,

        String endpoint,

        String httpMethod,

        Integer statusCode,

        Long responseTimeMs,

        Long userId,

        String clientIp,

        boolean slow,

        boolean error,

        LocalDateTime timestamp

) {


    public static ApiLogDto from(ApiLog log) {

        return new ApiLogDto(
                log.getId(),
                log.getServiceName(),
                log.getEndpoint(),
                log.getHttpMethod(),
                log.getStatusCode(),
                log.getResponseTimeMs(),
                log.getUserId(),
                log.getClientIp(),
                log.isSlow(),
                log.isError(),
                log.getTimestamp()
        );
    }
}