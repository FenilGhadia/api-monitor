package com.apimonitor.modules.monitoring.controller;

import com.apimonitor.common.exception.ApiException;
import com.apimonitor.common.response.ApiResponse;
import com.apimonitor.modules.monitoring.dto.ApiLogDto;
import com.apimonitor.modules.monitoring.dto.ApiLogFilterRequest;
import com.apimonitor.modules.monitoring.service.ApiLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class LogController {

    private final ApiLogService apiLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApiLogDto>>> getLogs(

            @RequestParam(required = false)
            String endpoint,

            @RequestParam(required = false)
            String httpMethod,

            @RequestParam(required = false)
            Integer statusCode,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @RequestParam(required = false)
            Long userId,

            @RequestParam(defaultValue = "false")
            Boolean slowOnly,

            @RequestParam(defaultValue = "false")
            Boolean errorOnly,

            @RequestParam(required = false)
            String serviceName,

            @PageableDefault(
                    size = 20,
                    sort = "timestamp",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        if (fromDate != null &&
                toDate != null &&
                fromDate.isAfter(toDate)) {

            throw new ApiException(
                    "fromDate must not be after toDate",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (pageable.getPageSize() > 100) {

            throw new ApiException(
                    "Page size must not exceed 100",
                    HttpStatus.BAD_REQUEST
            );
        }

        ApiLogFilterRequest filter = new ApiLogFilterRequest(
                endpoint,
                httpMethod,
                statusCode,
                fromDate,
                toDate,
                userId,
                slowOnly,
                errorOnly,
                serviceName
        );

        Page<ApiLogDto> page =
                apiLogService.getLogs(filter, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Logs retrieved", page)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiLogDto>> getLogById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Log entry retrieved",
                        apiLogService.getById(id)
                )
        );
    }
}