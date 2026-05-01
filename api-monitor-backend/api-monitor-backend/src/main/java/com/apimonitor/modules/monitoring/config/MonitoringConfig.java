package com.apimonitor.modules.monitoring.config;

import com.apimonitor.modules.monitoring.interceptor.ApiLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the ApiLoggingInterceptor with Spring MVC's interceptor chain.
 *
 * This configuration is self-contained within the monitoring module —
 * no other module needs to be modified to enable/disable logging.
 *
 * The interceptor applies to ALL paths ("/**") and uses its own
 * internal exclusion list for fine-grained control.
 */
@Configuration
@RequiredArgsConstructor
public class MonitoringConfig implements WebMvcConfigurer {

    private final ApiLoggingInterceptor apiLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiLoggingInterceptor)
                .addPathPatterns("/**");   // Capture everything; interceptor decides what to skip
    }
}