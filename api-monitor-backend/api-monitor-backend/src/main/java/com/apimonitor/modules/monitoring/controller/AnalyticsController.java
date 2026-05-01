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

/**
 * REST controller for all analytics/charting data.
 *
 * All endpoints:
 *  - ADMIN only
 *  - Accept ?from=&to= query params for time windowing
 *  - Default to "last 24 hours" when from/to are absent
 *
 * Routes:
 *  GET /api/v1/analytics/summary              → dashboard overview cards
 *  GET /api/v1/analytics/endpoints            → per-endpoint stats table
 *  GET /api/v1/analytics/endpoints/top        → N most-used endpoints
 *  GET /api/v1/analytics/endpoints/slowest    → N slowest endpoints
 *  GET /api/v1/analytics/timeseries/hourly    → hourly trend chart data
 *  GET /api/v1/analytics/timeseries/daily     → daily trend chart data
 *  GET /api/v1/analytics/status-codes         → status code distribution
 */
@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private static final int MAX_HOURLY_RANGE_HOURS = 7 * 24;
    private static final int MAX_DAILY_RANGE_DAYS   = 365;

    // ── Summary ──────────────────────────────────────────────────────────

    /**
     * Top-level dashboard metrics: total requests, avg response time,
     * error rate, slow request count, and alert status.
     */
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

    // ── Endpoint analytics ────────────────────────────────────────────────

    /**
     * Full per-endpoint stats table: all endpoints in the time window,
     * sorted by total requests descending.
     */
    @GetMapping("/endpoints")
    public ResponseEntity<ApiResponse<List<EndpointStatsDto>>> getEndpointStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        TimeWindow window = TimeWindow.resolve(from, to);
        return ResponseEntity.ok(
                ApiResponse.success(analyticsService.getEndpointStats(window.from(), window.to())));
    }

    /**
     * Top N most-used endpoints.
     * Default N = 10; can be overridden with ?limit=5
     */
    @GetMapping("/endpoints/top")
    public ResponseEntity<ApiResponse<List<EndpointStatsDto>>> getTopEndpoints(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "10") int limit) {

        // Fix #8: clamp limit — prevents memory exhaustion from ?limit=999999
        if (limit < 1 || limit > 100) {
            throw new ApiException("limit must be between 1 and 100", HttpStatus.BAD_REQUEST);
        }
        TimeWindow window = TimeWindow.resolve(from, to);
        return ResponseEntity.ok(
                ApiResponse.success(analyticsService.getTopEndpoints(window.from(), window.to(), limit)));
    }

    /**
     * Top N slowest endpoints by average response time.
     */
    @GetMapping("/endpoints/slowest")
    public ResponseEntity<ApiResponse<List<EndpointStatsDto>>> getSlowestEndpoints(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "10") int limit) {

        // Fix #8: clamp limit — prevents PageRequest.of(0, 999999) DB abuse
        if (limit < 1 || limit > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 100");
        }
        TimeWindow window = TimeWindow.resolve(from, to);
        return ResponseEntity.ok(
                ApiResponse.success(analyticsService.getSlowestEndpoints(window.from(), window.to(), limit)));
    }

    // ── Time-series ───────────────────────────────────────────────────────

    /**
     * Hourly bucketed trend data — best for "last 24h" charts.
     * Returns list of { bucket, requestCount, avgResponseMs, errorCount }.
     */
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

    /**
     * Daily bucketed trend data — best for "last 7d / 30d" charts.
     */
    @GetMapping("/timeseries/daily")
    public ResponseEntity<ApiResponse<List<TimeSeriesDto>>> getDailyTimeSeries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        // Default to last 7 days for daily view
        TimeWindow window = TimeWindow.resolve(from, to, 7 * 24);
        if (java.time.Duration.between(window.from(), window.to()).toDays() > MAX_DAILY_RANGE_DAYS) {
            throw new ApiException(
                    "Daily time-series range must not exceed 365 days", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(
                ApiResponse.success(analyticsService.getDailyTimeSeries(window.from(), window.to())));
    }

    // ── Status code distribution ──────────────────────────────────────────

    /**
     * Status code frequency breakdown for pie/bar chart.
     */
    @GetMapping("/status-codes")
    public ResponseEntity<ApiResponse<List<StatusCodeDistributionDto>>> getStatusCodeDistribution(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        TimeWindow window = TimeWindow.resolve(from, to);
        return ResponseEntity.ok(
                ApiResponse.success(analyticsService.getStatusCodeDistribution(window.from(), window.to())));
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    /**
     * Encapsulates time-window defaulting logic.
     *
     * When from/to are absent → defaults to "last 24 hours".
     * This private record is local to the controller — no leakage to other layers.
     */
    private record TimeWindow(LocalDateTime from, LocalDateTime to) {

        static TimeWindow resolve(LocalDateTime from, LocalDateTime to) {
            return resolve(from, to, 24);  // default: last 24 hours
        }

        static TimeWindow resolve(LocalDateTime from, LocalDateTime to, int defaultHours) {
            // Fix #9: use UTC explicitly — LocalDateTime.now() uses JVM default timezone
            // which may differ from the DB's UTC storage, causing off-by-offset windows
            LocalDateTime resolvedTo   = to   != null ? to   : LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime resolvedFrom = from != null ? from : resolvedTo.minusHours(defaultHours);
            return new TimeWindow(resolvedFrom, resolvedTo);
        }
    }
}