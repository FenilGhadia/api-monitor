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

/**
 * HTTP request logging interceptor.
 *
 * Lifecycle:
 *  preHandle       → records request start time into request attribute
 *  afterCompletion → computes elapsed time, extracts metadata, delegates
 *                    to ApiLogService.saveAsync() (non-blocking)
 *
 * Why afterCompletion (not postHandle)?
 *  postHandle does not fire on exception paths (4xx/5xx from exception handlers).
 *  afterCompletion always fires — even when the handler threw an exception —
 *  so we capture error responses correctly.
 *
 * Thread safety: request attributes are per-request; no shared state.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiLoggingInterceptor implements HandlerInterceptor {

    /** Request attribute key for the start timestamp */
    private static final String START_TIME_ATTR = "X-Request-Start-Time";

    private final ApiLogService       apiLogService;
    private final MonitoringProperties monitoringProperties;

    // ── Pre-handle: stamp the request ─────────────────────────────────────

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        // Record wall-clock start time for response-time computation
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true; // Always continue processing
    }

    // ── After completion: collect metrics and persist ─────────────────────

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {

        String path = request.getRequestURI();

        // Skip excluded paths (auth endpoints, self, actuator)
        if (isExcluded(path)) {
            return;
        }

        try {
            long startTime      = getStartTime(request);
            if (startTime < 0) return;
            long responseTimeMs = System.currentTimeMillis() - startTime;
            int  statusCode     = response.getStatus();

            // Resolve the authenticated user's ID from SecurityContext (may be null)
            Long userId = resolveUserId();

            // Truncate User-Agent to avoid column overflow
            String userAgent = truncate(request.getHeader("User-Agent"), 500);

            // Delegate to async service — this returns immediately
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
            // CRITICAL: logging must never break the main request flow
            log.error("Failed to capture API log for [{}]: {}", path, logEx.getMessage());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Returns true if the request path starts with any excluded prefix.
     */
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

    /**
     * Extracts the numeric user ID from the Spring SecurityContext.
     * Returns null for anonymous/unauthenticated requests.
     *
     * Uses Java 21 pattern matching instanceof.
     */
    private Long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        // Principal is our User entity (implements UserDetails)
        if (auth.getPrincipal() instanceof com.apimonitor.modules.user.entity.User user) {
            return user.getId();
        }
        return null;
    }

    /**
     * Extracts the real client IP, respecting X-Forwarded-For (reverse proxies).
     * Falls back to getRemoteAddr() if the header is absent.
     */
    // ApiLoggingInterceptor.java — replace resolveClientIp entirely
    private String resolveClientIp(HttpServletRequest request) {
        // server.forward-headers-strategy=framework means Spring has already
        // resolved the real client IP from X-Forwarded-For into getRemoteAddr().
        return request.getRemoteAddr();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}