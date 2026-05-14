package com.apimonitor.modules.monitoring.service;

import com.apimonitor.common.exception.ApiException;
import com.apimonitor.modules.monitoring.config.MonitoringProperties;
import com.apimonitor.modules.monitoring.dto.ApiLogDto;
import com.apimonitor.modules.monitoring.dto.ApiLogFilterRequest;
import com.apimonitor.modules.monitoring.dto.ExternalLogRequest;
import com.apimonitor.modules.monitoring.entity.ApiLog;
import com.apimonitor.modules.monitoring.repository.ApiLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiLogService {

    private final ApiLogRepository apiLogRepository;
    private final MonitoringProperties monitoringProperties;


    @Async
    @Transactional
    public void saveAsync(
            String endpoint,
            String httpMethod,
            int statusCode,
            long responseTimeMs,
            Long userId,
            String clientIp,
            String userAgent
    ) {
        try {
            boolean isSlow = responseTimeMs >= monitoringProperties.getSlowRequestThresholdMs();
            boolean isError = statusCode >= 400;

            ApiLog logEntry = ApiLog.builder()
                    .serviceName(null)
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

            log.debug(
                    "Logged [{}] {} → {} ({}ms)",
                    httpMethod,
                    endpoint,
                    statusCode,
                    responseTimeMs
            );

        } catch (Exception e) {
            log.warn(
                    "Async log write failed for [{} {}]: {}",
                    httpMethod,
                    endpoint,
                    e.getMessage()
            );
        }
    }


    @Transactional
    public ApiLogDto saveExternal(ExternalLogRequest request) {

        boolean isSlow =
                request.responseTimeMs() >= monitoringProperties.getSlowRequestThresholdMs();

        boolean isError =
                request.statusCode() >= 400;

        LocalDateTime effectiveTimestamp =
                request.timestamp() != null
                        ? request.timestamp()
                        : LocalDateTime.now();

        ApiLog logEntry = ApiLog.builder()
                .serviceName(truncate(request.serviceName(), 100))
                .endpoint(truncate(request.endpoint(), 500))
                .httpMethod(request.httpMethod().toUpperCase())
                .statusCode(request.statusCode())
                .responseTimeMs(request.responseTimeMs())
                .slow(isSlow)
                .error(isError)
                .build();

        logEntry.setTimestamp(effectiveTimestamp);

        ApiLog saved = apiLogRepository.save(logEntry);

        log.debug(
                "External log saved: [{}] {} {} → {} ({}ms)",
                request.serviceName(),
                request.httpMethod(),
                request.endpoint(),
                request.statusCode(),
                request.responseTimeMs()
        );

        return toDto(saved);
    }


    @Transactional(readOnly = true)
    public Page<ApiLogDto> getLogs(ApiLogFilterRequest filter, Pageable pageable) {

        Specification<ApiLog> spec = (root, query, cb) -> {

            var predicates = new ArrayList<Predicate>();

            if (filter.endpoint() != null && !filter.endpoint().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("endpoint")),
                                "%" + filter.endpoint().toLowerCase() + "%"
                        )
                );
            }

            if (filter.httpMethod() != null) {
                predicates.add(
                        cb.equal(root.get("httpMethod"), filter.httpMethod())
                );
            }

            if (filter.statusCode() != null) {
                predicates.add(
                        cb.equal(root.get("statusCode"), filter.statusCode())
                );
            }

            if (filter.fromDate() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("timestamp"),
                                filter.fromDate()
                        )
                );
            }

            if (filter.toDate() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("timestamp"),
                                filter.toDate()
                        )
                );
            }

            if (filter.userId() != null) {
                predicates.add(
                        cb.equal(root.get("userId"), filter.userId())
                );
            }

            if (Boolean.TRUE.equals(filter.slowOnly())) {
                predicates.add(
                        cb.isTrue(root.get("slow"))
                );
            }

            if (Boolean.TRUE.equals(filter.errorOnly())) {
                predicates.add(
                        cb.isTrue(root.get("error"))
                );
            }

            if (filter.serviceName() != null &&
                    !filter.serviceName().isBlank()) {

                predicates.add(
                        cb.equal(
                                root.get("serviceName"),
                                filter.serviceName()
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return apiLogRepository.findAll(spec, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ApiLogDto getById(Long id) {

        return apiLogRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() ->
                        new ApiException(
                                "Log entry not found: " + id,
                                HttpStatus.NOT_FOUND
                        )
                );
    }


    private ApiLogDto toDto(ApiLog log) {

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

    private String truncate(String value, int max) {

        if (value == null) {
            return null;
        }

        return value.length() > max
                ? value.substring(0, max)
                : value;
    }
}