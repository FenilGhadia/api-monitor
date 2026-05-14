package com.apimonitor.modules.monitoring.interceptor;

import com.apimonitor.modules.monitoring.config.MonitoringProperties;
import com.apimonitor.modules.monitoring.service.ApiLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Slf4j
@Component
@RequiredArgsConstructor
public class ApiLoggingInterceptor implements HandlerInterceptor {


    private static final String START_TIME_ATTR = "X-Request-Start-Time";

    private final ApiLogService       apiLogService;
    private final MonitoringProperties monitoringProperties;



    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {


        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }



    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {

        String path = request.getRequestURI();


        if (isExcluded(path)) {
            return;
        }

        try {
            long startTime      = getStartTime(request);
            if (startTime < 0) return;
            long responseTimeMs = System.currentTimeMillis() - startTime;
            int  statusCode     = response.getStatus();


            Long userId = resolveUserId();


            String userAgent = truncate(request.getHeader("User-Agent"), 500);


            apiLogService.saveAsync(
                    path,
                    request.getMethod(),
                    statusCode,
                    responseTimeMs,
                    userId,
                    resolveClientIp(request),
                    userAgent
            );
        } catch (Exception logEx) {

            log.error("Failed to capture API log for [{}]: {}", path, logEx.getMessage());
        }
    }




    private boolean isExcluded(String path) {
        return monitoringProperties.getExcludedPaths().stream()
                .anyMatch(path::startsWith);
    }

    private long getStartTime(HttpServletRequest request) {
        Object attr = request.getAttribute(START_TIME_ATTR);
        if (attr instanceof Long startMs) {
            return startMs;
        }
        log.warn("Start time attribute missing for [{}] — response time will not be recorded",
                request.getRequestURI());
        return -1L;
    }


    private Long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }


        if (auth.getPrincipal() instanceof com.apimonitor.modules.user.entity.User user) {
            return user.getId();
        }
        return null;
    }


    private String resolveClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}