package com.apimonitor.modules.monitoring.controller;

import com.apimonitor.common.exception.ApiException;
import com.apimonitor.common.response.ApiResponse;
import com.apimonitor.modules.monitoring.dto.AnalyticsSummaryDto;
import com.apimonitor.modules.monitoring.dto.EndpointStatsDto;
import com.apimonitor.modules.monitoring.dto.StatusCodeDistributionDto;
import com.apimonitor.modules.monitoring.dto.TimeSeriesDto;
import com.apimonitor.modules.monitoring.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;


@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private static final int MAX_HOURLY_RANGE_HOURS = 7 * 24;
    private static final int MAX_DAILY_RANGE_DAYS   = 365;



    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AnalyticsSummaryDto>> getSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to) {

        TimeWindow window = TimeWindow.resolve(from, to);
        AnalyticsSummaryDto summary = analyticsService.getSummary(window.from(), window.to());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }




    @GetMapping("/endpoints")
    public ResponseEntity<ApiResponse<List<EndpointStatsDto>>> getEndpointStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        TimeWindow window = TimeWindow.resolve(from, to);
        return ResponseEntity.ok(
                ApiResponse.success(analyticsService.getEndpointStats(window.from(), window.to())));
    }


    @GetMapping("/endpoints/top")
    public ResponseEntity<ApiResponse<List<EndpointStatsDto>>> getTopEndpoints(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "10") int limit) {


        if (limit < 1 || limit > 100) {
            throw new ApiException("limit must be between 1 and 100", HttpStatus.BAD_REQUEST);
        }
        TimeWindow window = TimeWindow.resolve(from, to);
        return ResponseEntity.ok(
                ApiResponse.success(analyticsService.getTopEndpoints(window.from(), window.to(), limit)));
    }


    @GetMapping("/endpoints/slowest")
    public ResponseEntity<ApiResponse<List<EndpointStatsDto>>> getSlowestEndpoints(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "10") int limit) {


        if (limit < 1 || limit > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 100");
        }
        TimeWindow window = TimeWindow.resolve(from, to);
        return ResponseEntity.ok(
                ApiResponse.success(analyticsService.getSlowestEndpoints(window.from(), window.to(), limit)));
    }




    @GetMapping("/timeseries/hourly")
    public ResponseEntity<ApiResponse<List<TimeSeriesDto>>> getHourlyTimeSeries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        TimeWindow window = TimeWindow.resolve(from, to);
        if (java.time.Duration.between(window.from(), window.to()).toHours() > MAX_HOURLY_RANGE_HOURS) {
            throw new ApiException(
                    "Hourly time-series range must not exceed 7 days", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(
                ApiResponse.success(analyticsService.getHourlyTimeSeries(window.from(), window.to())));
    }


    @GetMapping("/timeseries/daily")
    public ResponseEntity<ApiResponse<List<TimeSeriesDto>>> getDailyTimeSeries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {


        TimeWindow window = TimeWindow.resolve(from, to, 7 * 24);
        if (java.time.Duration.between(window.from(), window.to()).toDays() > MAX_DAILY_RANGE_DAYS) {
            throw new ApiException(
                    "Daily time-series range must not exceed 365 days", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(
                ApiResponse.success(analyticsService.getDailyTimeSeries(window.from(), window.to())));
    }




    @GetMapping("/status-codes")
    public ResponseEntity<ApiResponse<List<StatusCodeDistributionDto>>> getStatusCodeDistribution(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        TimeWindow window = TimeWindow.resolve(from, to);
        return ResponseEntity.ok(
                ApiResponse.success(analyticsService.getStatusCodeDistribution(window.from(), window.to())));
    }




    private record TimeWindow(LocalDateTime from, LocalDateTime to) {

        static TimeWindow resolve(LocalDateTime from, LocalDateTime to) {
            return resolve(from, to, 24);
        }

        static TimeWindow resolve(LocalDateTime from, LocalDateTime to, int defaultHours) {

            LocalDateTime resolvedTo   = to   != null ? to   : LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime resolvedFrom = from != null ? from : resolvedTo.minusHours(defaultHours);
            return new TimeWindow(resolvedFrom, resolvedTo);
        }
    }
}