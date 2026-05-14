package com.apimonitor.modules.monitoring.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;


public record ApiLogFilterRequest(

        String endpoint,

        String httpMethod,

        Integer statusCode,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime fromDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime toDate,

        Long userId,

        Boolean slowOnly,

        Boolean errorOnly,

        String serviceName

) {


    public ApiLogFilterRequest() {

        this(
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                null
        );
    }
}