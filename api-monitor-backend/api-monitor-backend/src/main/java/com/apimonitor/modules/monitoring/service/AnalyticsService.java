package com.apimonitor.modules.monitoring.service;

import com.apimonitor.modules.monitoring.config.MonitoringProperties;
import com.apimonitor.modules.monitoring.dto.AnalyticsSummaryDto;
import com.apimonitor.modules.monitoring.dto.EndpointStatsDto;
import com.apimonitor.modules.monitoring.dto.StatusCodeDistributionDto;
import com.apimonitor.modules.monitoring.dto.TimeSeriesDto;
import com.apimonitor.modules.monitoring.entity.ApiLog;
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


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ApiLogRepository     apiLogRepository;
    private final MonitoringProperties monitoringProperties;



    public AnalyticsSummaryDto getSummary(LocalDateTime from, LocalDateTime to) {

        if (from == null || to == null) {
            from = LocalDateTime.now().minusDays(30);
            to   = LocalDateTime.now();
        }


        Object[] stats = apiLogRepository.getSummaryStats(from, to);

        LocalDateTime fromVal = (from != null) ? from : LocalDateTime.now().minusDays(30);
        LocalDateTime toVal   = (to   != null) ? to   : LocalDateTime.now();

        List<ApiLog> logs = apiLogRepository.findAll().stream()
                .filter(log -> !log.getTimestamp().isBefore(fromVal) &&
                        !log.getTimestamp().isAfter(toVal))
                .toList();

        long total = logs.size();

        double avgTime = logs.stream()
                .mapToLong(ApiLog::getResponseTimeMs)
                .average()
                .orElse(0.0);

        long errorCount = logs.stream()
                .filter(l -> Boolean.TRUE.equals(l.isError()))
                .count();

        long slowCount = logs.stream()
                .filter(l -> Boolean.TRUE.equals(l.isSlow()))
                .count();

        double errorRate = total > 0
                ? ((double) errorCount / total) * 100.0
                : 0.0;


        MonitoringProperties.Alert thresholds = monitoringProperties.getAlert();
        List<String> alertMessages = new ArrayList<>();


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


    public List<EndpointStatsDto> getEndpointStats(LocalDateTime from, LocalDateTime to) {

        return apiLogRepository.findEndpointStats(from, to, PageRequest.of(0, 200))
                .stream()
                .map(EndpointStatsDto::fromRow)
                .toList();
    }

    public List<EndpointStatsDto> getTopEndpoints(LocalDateTime from,
                                                  LocalDateTime to,
                                                  int limit) {
        // Re-use findEndpointStats (already ordered by total desc) and truncate
        return getEndpointStats(from, to).stream()
                .limit(limit)
                .toList();
    }


    public List<EndpointStatsDto> getSlowestEndpoints(LocalDateTime from,
                                                      LocalDateTime to,
                                                      int limit) {

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


    public List<TimeSeriesDto> getHourlyTimeSeries(LocalDateTime from, LocalDateTime to) {
        return apiLogRepository.findHourlyStats(from, to)
                .stream()
                .map(TimeSeriesDto::fromRow)
                .toList();
    }


    public List<TimeSeriesDto> getDailyTimeSeries(LocalDateTime from, LocalDateTime to) {
        return apiLogRepository.findDailyStats(from, to)
                .stream()
                .map(TimeSeriesDto::fromRow)
                .toList();
    }


    public List<StatusCodeDistributionDto> getStatusCodeDistribution(
            LocalDateTime from, LocalDateTime to) {
        return apiLogRepository.findStatusCodeDistribution(from, to)
                .stream()
                .map(StatusCodeDistributionDto::fromRow)
                .toList();
    }
}