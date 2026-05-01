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

/**
 * REST controller for the log viewer feature.
 *
 * All endpoints are ADMIN-only — log data contains IP addresses
 * and user IDs and must not be exposed to regular users.
 * (Also enforced at SecurityConfig level.)
 *
 * Routes:
 *  GET /api/v1/logs              → paginated list with optional filters
 *  GET /api/v1/logs/{id}         → single log entry detail
 *
 * Filtering via query parameters (all optional):
 *  ?endpoint=/api/v1/products
 *  &httpMethod=GET
 *  &statusCode=500
 *  &fromDate=2024-01-01T00:00:00
 *  &toDate=2024-01-02T00:00:00
 *  &userId=42
 *  &slowOnly=true
 *  &errorOnly=true
 *  &page=0&size=20&sort=timestamp,desc
 */
@RestController
@RequestMapping("/api/v1/logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class LogController {

    private final ApiLogService apiLogService;

    /**
     * Returns a paginated, filtered page of API logs.
     *
     * Spring binds each query param individually — this avoids
     * requiring the client to send a JSON body for a GET request.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApiLogDto>>> getLogs(

            @RequestParam(required = false) String endpoint,
            @RequestParam(required = false) String httpMethod,
            @RequestParam(required = false) Integer statusCode,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @RequestParam(required = false)      Long userId,
            @RequestParam(defaultValue = "false") Boolean slowOnly,
            @RequestParam(defaultValue = "false") Boolean errorOnly,

            // LogController.java — line ~55
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable) {

        // Assemble the filter record from individual query params
        ApiLogFilterRequest filter = new ApiLogFilterRequest(
                endpoint, httpMethod, statusCode,
                fromDate, toDate,
                userId, slowOnly, errorOnly
        );
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ApiException("fromDate must not be after toDate", HttpStatus.BAD_REQUEST);
        }

        Page<ApiLogDto> page = apiLogService.getLogs(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Logs retrieved", page));
    }

    /**
     * Fetches a single log entry for the detail/drill-down view.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiLogDto>> getLogById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Log entry retrieved", apiLogService.getById(id)));
    }
}