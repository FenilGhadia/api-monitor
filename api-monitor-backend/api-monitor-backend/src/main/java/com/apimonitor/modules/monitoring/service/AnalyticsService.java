package com.apimonitor.modules.monitoring.service;

import com.apimonitor.modules.monitoring.config.MonitoringProperties;
import com.apimonitor.modules.monitoring.dto.AnalyticsSummaryDto;
import com.apimonitor.modules.monitoring.dto.EndpointStatsDto;
import com.apimonitor.modules.monitoring.dto.StatusCodeDistributionDto;
import com.apimonitor.modules.monitoring.dto.TimeSeriesDto;
import com.apimonitor.modules.monitoring.repository.ApiLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes all analytics aggregations consumed by the dashboard.
 *
 * Every method accepts a time window (from / to) so the frontend
 * can request "last 1h", "last 24h", "last 7d" dynamically.
 *
 * All methods are read-only transactions — no writes occur here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ApiLogRepository     apiLogRepository;
    private final MonitoringProperties monitoringProperties;

    // ── Summary (dashboard overview cards) ────────────────────────────────

    /**
     * Computes the top-level metrics for the dashboard overview.
     *
     * Includes alert evaluation: if error rate or avg response time
     * exceeds configured thresholds, alertTriggered = true and a
     * human-readable message is set.
     *
     * @param from  start of the time window (inclusive)
     * @param to    end of the time window (inclusive)
     */
    public AnalyticsSummaryDto getSummary(LocalDateTime from, LocalDateTime to) {
        Object[] stats = apiLogRepository.getSummaryStats(from, to);

        /*
         * Null-safety rationale:
         *
         * Outer guard (stats != null && stats.length >= 4):
         *   JPQL aggregate queries without GROUP BY always produce exactly one row,
         *   BUT some Hibernate / Spring Data versions return null (not a 1-row result)
         *   when the underlying table is completely empty. Accessing stats[0] on a
         *   null reference → NullPointerException → 500. Safe defaults are used instead.
         *
         * Per-element guards (stats[n] != null):
         *  stats[0] — COUNT(l)            → 0 on empty table, but guard for safety
         *  stats[1] — COALESCE(AVG, 0.0)  → COALESCE handles it, guard as defence-in-depth
         *  stats[2] — SUM(CASE WHEN slow) → SQL NULL on empty table (no COALESCE in query)
         *  stats[3] — SUM(CASE WHEN error)→ SQL NULL on empty table (no COALESCE in query)
         *
         * ((Number) value).longValue() / .doubleValue() avoids ClassCastException across
         * JPA providers that may return BigInteger, BigDecimal, Long, or Double for the same
         * aggregate expression.
         */
        long   total      = 0L;
        double avgTime    = 0.0;
        long   errorCount = 0L;
        long   slowCount  = 0L;

        if (stats != null && stats.length >= 4) {
            total      = stats[0] != null ? ((Number) stats[0]).longValue()   : 0L;
            avgTime    = stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
            errorCount = stats[2] != null ? ((Number) stats[2]).longValue()   : 0L;
            slowCount  = stats[3] != null ? ((Number) stats[3]).longValue()   : 0L;
        }

        double errorRate = total > 0
                ? ((double) errorCount / total) * 100.0
                : 0.0;

        // ── Alert evaluation ──
        MonitoringProperties.Alert thresholds = monitoringProperties.getAlert();
        List<String> alertMessages = new ArrayList<>();

        // Fix #10: > not >= — alert fires strictly above threshold, not exactly at it
        if (errorRate > thresholds.getErrorRateThreshold()) {
            alertMessages.add(
                    "High error rate: %.1f%% (threshold: %.1f%%)".formatted(
                            errorRate, thresholds.getErrorRateThreshold()));
        }
        if (avgTime > thresholds.getResponseTimeThresholdMs()) {
            alertMessages.add(
                    "High avg response time: %.0fms (threshold: %dms)".formatted(
                            avgTime, thresholds.getResponseTimeThresholdMs()));
        }

        boolean alertTriggered = !alertMessages.isEmpty();
        String  alertMessage   = alertTriggered
                ? String.join(" | ", alertMessages)
                : null;

        if (alertTriggered) {
            log.warn("Alert triggered: {}", alertMessage);
        }

        return new AnalyticsSummaryDto(
                total,
                avgTime,
                errorRate,
                slowCount,
                errorCount,
                alertTriggered,
                alertMessage
        );
    }

    // ── Endpoint analytics ────────────────────────────────────────────────

    /**
     * Returns per-endpoint stats for all endpoints active in the time window.
     * Ordered by total requests descending.
     */
    public List<EndpointStatsDto> getEndpointStats(LocalDateTime from, LocalDateTime to) {
        // Fix #4: hard cap of 200 — prevents unbounded full-table scan on systems
        // with hundreds of distinct endpoints
        return apiLogRepository.findEndpointStats(from, to, PageRequest.of(0, 200))
                .stream()
                .map(EndpointStatsDto::fromRow)
                .toList();
    }

    /**
     * Returns the top N most-used endpoints.
     */
    public List<EndpointStatsDto> getTopEndpoints(LocalDateTime from,
                                                  LocalDateTime to,
                                                  int limit) {
        // Re-use findEndpointStats (already ordered by total desc) and truncate
        return getEndpointStats(from, to).stream()
                .limit(limit)
                .toList();
    }

    /**
     * Returns the top N slowest endpoints by average response time.
     */
    public List<EndpointStatsDto> getSlowestEndpoints(LocalDateTime from,
                                                      LocalDateTime to,
                                                      int limit) {
        // Fix #3: sort expressed via PageRequest so it doesn't conflict with any
        // ORDER BY clause in the query (Hibernate 6 dual-sort bug)
        return apiLogRepository.findSlowestEndpoints(
                        from, to,
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "avgTime")))
                .stream()
                .map(row -> {
                    String endpoint = (String)  row[0];
                    double avgTime  = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                    long   count    = ((Number) row[2]).longValue();
                    return new EndpointStatsDto(endpoint, count, avgTime, 0L, 0L, 0.0);
                })
                .toList();
    }

    // ── Time-series analytics ─────────────────────────────────────────────

    /**
     * Hourly bucketed request/error/avgTime data for the trend chart.
     * Typically used for "last 24 hours" view.
     */
    public List<TimeSeriesDto> getHourlyTimeSeries(LocalDateTime from, LocalDateTime to) {
        return apiLogRepository.findHourlyStats(from, to)
                .stream()
                .map(TimeSeriesDto::fromRow)
                .toList();
    }

    /**
     * Daily bucketed data for the trend chart.
     * Typically used for "last 7 / 30 days" view.
     */
    public List<TimeSeriesDto> getDailyTimeSeries(LocalDateTime from, LocalDateTime to) {
        return apiLogRepository.findDailyStats(from, to)
                .stream()
                .map(TimeSeriesDto::fromRow)
                .toList();
    }

    // ── Status code distribution ──────────────────────────────────────────

    /**
     * Status code frequency for pie/bar chart.
     */
    public List<StatusCodeDistributionDto> getStatusCodeDistribution(
            LocalDateTime from, LocalDateTime to) {
        return apiLogRepository.findStatusCodeDistribution(from, to)
                .stream()
                .map(StatusCodeDistributionDto::fromRow)
                .toList();
    }
}