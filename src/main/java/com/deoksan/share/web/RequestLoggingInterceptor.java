package com.deoksan.share.web;

import com.deoksan.share.config.logging.MdcKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final int SHORT_TRACE_ID_LENGTH = 8;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // MdcLoggingFilter가 이미 설정했을 수 있으므로, 없을 때만 생성
        if (MDC.get(MdcKey.TRACE_ID) == null) {
            MDC.put(MdcKey.TRACE_ID, UUID.randomUUID().toString().substring(0, SHORT_TRACE_ID_LENGTH));
        }
        MDC.put(MdcKey.REQUEST_URI, request.getRequestURI());
        MDC.put(MdcKey.HTTP_METHOD, request.getMethod());

        log.info("[REQUEST] {} {} from {}",
                request.getMethod(),
                maskSensitiveParams(request),
                request.getRemoteAddr());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        log.info("[RESPONSE] {} {} -> {} ({}ms)",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                getProcessingTime(request));
        MDC.clear();
    }

    private long getProcessingTime(HttpServletRequest request) {
        Long startTime = (Long) request.getAttribute("startTime");
        return startTime != null ? System.currentTimeMillis() - startTime : -1;
    }

    private String maskSensitiveParams(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query == null) return uri;
        return uri + "?" + query.replaceAll("(?i)(token|code|key|secret|password)=([^&]+)", "$1=***");
    }
}
