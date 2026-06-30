package com.deoksan.share.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.deoksan.share.util.LogSanitizer;
import com.deoksan.share.web.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP 요청 진입/종료 시 MDC traceId 를 생성·정리하고
 * 요청-응답 bookend 로그를 남기는 OncePerRequestFilter.
 */
@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MdcLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = UUID.randomUUID().toString().replace("-", "");

        // X-Correlation-Id: 클라이언트가 보낸 값을 이어받거나, 없으면 traceId로 대체.
        // 서비스 간 호출 시 이 헤더를 전달하면 분산 환경에서도 흐름 추적 가능.
        // 외부 헤더값 → MDC 전 로그 인젝션 정제 (CWE-117)
        String rawCorrelationId = request.getHeader(HttpHeader.X_CORRELATION_ID);
        String correlationId = (rawCorrelationId == null || rawCorrelationId.isBlank())
                ? traceId
                : LogSanitizer.clean(rawCorrelationId, 64);

        // CF-Ray: Cloudflare 엣지 요청 ID. 없으면 "-" (직접 요청)
        String cfRaw = request.getHeader(HttpHeader.CF_RAY);
        String cfRay = (cfRaw != null && !cfRaw.isBlank())
                ? LogSanitizer.clean(cfRaw, 32)
                : "-";

        MDC.put(MdcKey.TRACE_ID, traceId);
        MDC.put(MdcKey.CORRELATION_ID, correlationId);
        MDC.put(MdcKey.CF_RAY, cfRay);

        try {
            log.info(">> {} {} [traceId={} correlationId={} cfRay={}]",
                    request.getMethod(), maskSensitiveParams(request), traceId, correlationId, cfRay);
            filterChain.doFilter(request, response);
        } finally {
            log.info("<< {} {} {} [traceId={}]",
                    request.getMethod(), maskSensitiveParams(request),
                    response.getStatus(), traceId);
            MDC.clear();
        }
    }

    // token=, code= 등 보안 파라미터가 쿼리스트링에 포함되면 마스킹
    // → 로그 파일에 일회용 토큰이 평문으로 남는 것을 방지 (ISO 27002 A.8.12)
    private String maskSensitiveParams(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query == null) return uri;
        String masked = query.replaceAll(
                "(?i)(token|code|key|secret|password)=([^&]+)",
                "$1=***"
        );
        return uri + "?" + masked;
    }
}
