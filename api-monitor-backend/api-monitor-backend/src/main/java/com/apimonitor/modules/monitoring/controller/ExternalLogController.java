package com.apimonitor.modules.monitoring.controller;

import com.apimonitor.common.response.ApiResponse;
import com.apimonitor.modules.monitoring.dto.ApiLogDto;
import com.apimonitor.modules.monitoring.dto.ExternalLogRequest;
import com.apimonitor.modules.monitoring.service.ApiLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/external/logs")
@RequiredArgsConstructor
public class ExternalLogController {

    private final ApiLogService apiLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApiLogDto>> ingest(
            @Valid @RequestBody ExternalLogRequest request) {

        ApiLogDto saved = apiLogService.saveExternal(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Log ingested", saved));
    }
}