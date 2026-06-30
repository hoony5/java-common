package com.deoksan.share.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final long SLOW_REQUEST_THRESHOLD_MS = 1000;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute("startTime");
        if (startTime == null) return;

        long duration = System.currentTimeMillis() - startTime;
        if (duration > SLOW_REQUEST_THRESHOLD_MS) {
            log.warn("[SLOW] {} {} took {}ms (threshold: {}ms)",
                    request.getMethod(), request.getRequestURI(), duration, SLOW_REQUEST_THRESHOLD_MS);
        } else {
            log.debug("[PERF] {} {} took {}ms",
                    request.getMethod(), request.getRequestURI(), duration);
        }
    }
}
