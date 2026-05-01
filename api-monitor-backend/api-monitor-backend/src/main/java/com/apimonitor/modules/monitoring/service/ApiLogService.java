package com.apimonitor.modules.monitoring.service;

import com.apimonitor.modules.monitoring.config.MonitoringProperties;
import com.apimonitor.modules.monitoring.dto.ApiLogDto;
import com.apimonitor.modules.monitoring.dto.ApiLogFilterRequest;
import com.apimonitor.modules.monitoring.entity.ApiLog;
import com.apimonitor.modules.monitoring.repository.ApiLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;


/**
 * Service responsible for persisting API log entries.
 *
 * Key design: saveAsync() is annotated with @Async so the interceptor
 * returns immediately without waiting for the DB write. This ensures
 * zero latency impact on monitored endpoints.
 *
 * The async executor is configured in application.yml under spring.task.execution.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiLogService {

    private final ApiLogRepository     apiLogRepository;
    private final MonitoringProperties monitoringProperties;

    // ── Write path (async) ────────────────────────────────────────────────

    /**
     * Persists a single API log entry asynchronously.
     *
     * Called by ApiLoggingInterceptor after every non-excluded request.
     * The @Async annotation delegates execution to the async-log- thread pool.
     */
    @Async
    @Transactional
    public void saveAsync(
            String  endpoint,
            String  httpMethod,
            int     statusCode,
            long    responseTimeMs,
            Long    userId,
            String  clientIp,
            String  userAgent) {
        try {
            boolean isSlow  = responseTimeMs >= monitoringProperties.getSlowRequestThresholdMs();
            boolean isError = statusCode >= 400;

            ApiLog logEntry = ApiLog.builder()
                    .endpoint(endpoint)
                    .httpMethod(httpMethod)
                    .statusCode(statusCode)
                    .responseTimeMs(responseTimeMs)
                    .userId(userId)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .slow(isSlow)
                    .error(isError)
                    .build();

            apiLogRepository.save(logEntry);
            log.debug("Logged [{}] {} → {} ({}ms)", httpMethod, endpoint, statusCode, responseTimeMs);

        } catch (Exception e) {
            // Never propagate — logging must never affect the monitored application.
            // WARN level so ops teams can detect persistent DB connectivity issues.
            log.warn("Async log write failed for [{} {}]: {}", httpMethod, endpoint, e.getMessage());
        }
    }

    // ── Read path ─────────────────────────────────────────────────────────

    /**
     * Returns a paginated, filterable view of logs.
     * All filter parameters are optional — null/false means "all".
     */
    @Transactional(readOnly = true)
    public Page<ApiLogDto> getLogs(ApiLogFilterRequest filter, Pageable pageable) {

        Specification<ApiLog> spec = (root, query, cb) -> {
            var predicates = new ArrayList<>();

            if (filter.endpoint() != null && !filter.endpoint().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("endpoint")),
                                "%" + filter.endpoint().toLowerCase() + "%"
                        )
                );
            }

            if (filter.httpMethod() != null) {
                predicates.add(cb.equal(root.get("httpMethod"), filter.httpMethod()));
            }

            if (filter.statusCode() != null) {
                predicates.add(cb.equal(root.get("statusCode"), filter.statusCode()));
            }

            if (filter.fromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), filter.fromDate()));
            }

            if (filter.toDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), filter.toDate()));
            }

            if (filter.userId() != null) {
                predicates.add(cb.equal(root.get("userId"), filter.userId()));
            }

            if (Boolean.TRUE.equals(filter.slowOnly())) {
                predicates.add(cb.isTrue(root.get("slow")));
            }

            if (Boolean.TRUE.equals(filter.errorOnly())) {
                predicates.add(cb.isTrue(root.get("error")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return apiLogRepository.findAll(spec, pageable)
                .map(log -> new ApiLogDto(
                        log.getId(),
                        log.getEndpoint(),
                        log.getHttpMethod(),
                        log.getStatusCode(),
                        log.getResponseTimeMs(),
                        log.getUserId(),
                        log.getClientIp(),
                        log.isSlow(),
                        log.isError(),
                        log.getTimestamp()
                ));
    }

    /**
     * Returns a single log entry by ID for detail view.
     */
    @Transactional(readOnly = true)
    public ApiLogDto getById(Long id) {
        return apiLogRepository.findById(id)
                .map(ApiLogDto::from)
                .orElseThrow(() -> new com.apimonitor.common.exception.ApiException(
                        "Log entry not found: " + id,
                        org.springframework.http.HttpStatus.NOT_FOUND));
    }
}