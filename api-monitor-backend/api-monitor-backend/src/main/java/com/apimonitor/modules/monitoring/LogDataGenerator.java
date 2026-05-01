package com.apimonitor.modules.monitoring;

import com.apimonitor.modules.monitoring.entity.ApiLog;
import com.apimonitor.modules.monitoring.repository.ApiLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class LogDataGenerator {

    private final ApiLogRepository apiLogRepository;

    @PostConstruct
    public void generateData() {
        for (int i = 0; i < 200; i++) {
            ApiLog log = ApiLog.builder()
                    .endpoint("/api/test/" + (i % 5))
                    .httpMethod(i % 2 == 0 ? "GET" : "POST")
                    .statusCode(i % 10 == 0 ? 500 : 200)
                    .responseTimeMs((long) (Math.random() * 1000))
                    .userId(1L)
                    .clientIp("127.0.0.1")
                    .userAgent("Test-Agent")
                    .slow(Math.random() > 0.7)
                    .error(i % 10 == 0)
                    .timestamp(LocalDateTime.now().minusMinutes(i))
                    .build();

            apiLogRepository.save(log);
        }
    }
}