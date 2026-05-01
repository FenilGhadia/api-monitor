package com.apimonitor.modules.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Persistent record of a single HTTP request captured by ApiLoggingInterceptor.
 *
 * Design decisions:
 *  - No @LastModifiedDate — logs are immutable once written
 *  - clientIp and userAgent stored for audit / debugging
 *  - userId is nullable — unauthenticated requests are still logged
 *  - slow flag is computed and stored at write-time for fast dashboard queries
 *
 * Table indexes:
 *  - endpoint        → endpoint analytics queries
 *  - timestamp       → time-range filtering (most common query pattern)
 *  - statusCode      → error-rate queries
 *  - userId          → per-user activity lookup
 */
@Entity
@Table(
        name = "api_logs",
        indexes = {
                @Index(name = "idx_log_endpoint",   columnList = "endpoint"),
                @Index(name = "idx_log_timestamp",  columnList = "timestamp"),
                @Index(name = "idx_log_status",     columnList = "status_code"),
                @Index(name = "idx_log_user",       columnList = "user_id"),
                @Index(name = "idx_log_method",     columnList = "http_method")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The request path, e.g. /api/v1/products/42 */
    @Column(nullable = false, length = 500, columnDefinition = "VARCHAR(500)")
    private String endpoint;

    /** GET, POST, PUT, PATCH, DELETE, etc. */
    @Column(name = "http_method", nullable = false, length = 10, columnDefinition = "VARCHAR(10)")
    private String httpMethod;

    /** HTTP response status code: 200, 201, 400, 401, 403, 404, 500 ... */
    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    /** Wall-clock time from request receipt to response completion (ms) */
    @Column(name = "response_time_ms", nullable = false)
    private Long responseTimeMs;

    /** Populated from SecurityContext — null for unauthenticated requests */
    @Column(name = "user_id")
    private Long userId;

    /** Originating IP address */
    @Column(name = "client_ip", length = 50, columnDefinition = "VARCHAR(50)")
    private String clientIp;

    /** Truncated to 500 chars to avoid column bloat */
    @Column(name = "user_agent", length = 500, columnDefinition = "VARCHAR(500)")
    private String userAgent;

    /** Pre-computed: true if responseTimeMs > configured threshold (default 1000ms) */
    @Column(name = "is_slow", nullable = false)
    @Builder.Default
    private boolean slow = false;

    /** Pre-computed: true for 4xx/5xx responses — avoids BETWEEN query on every dashboard load */
    @Column(name = "is_error", nullable = false)
    @Builder.Default
    private boolean error = false;

    /** When the request was received */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}