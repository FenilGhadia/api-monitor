package com.apimonitor.modules.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "api_logs",
        indexes = {
                @Index(name = "idx_log_endpoint",     columnList = "endpoint"),
                @Index(name = "idx_log_timestamp",    columnList = "timestamp"),
                @Index(name = "idx_log_status",       columnList = "status_code"),
                @Index(name = "idx_log_user",         columnList = "user_id"),
                @Index(name = "idx_log_method",       columnList = "http_method"),
                @Index(name = "idx_log_service_name", columnList = "service_name")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "response_time_ms", nullable = false)
    private Long responseTimeMs;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "is_slow", nullable = false)
    @Builder.Default
    private boolean slow = false;

    @Column(name = "is_error", nullable = false)
    @Builder.Default
    private boolean error = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}