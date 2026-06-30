package com.deoksan.share.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.deoksan.share.web.HttpHeader;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 공통 응답 헤더를 주입하는 필터.
 *
 * <h3>추가 헤더</h3>
 * <pre>
 * X-Trace-Id      : MDC traceId → 클라이언트가 오류 리포트 시 제출
 * X-Api-Version   : API 버전 → 클라이언트 호환성 확인
 * Cache-Control   : 인증 엔드포인트는 no-store 강제 (토큰 응답 캐시 금지)
 * X-Request-Id    : 클라이언트 제출 ID를 에코 (없으면 생략)
 * </pre>
 *
 * <h3>Cache-Control 전략</h3>
 * <pre>
 * /api/auth/**        → no-store (토큰/세션 응답은 캐시 금지)
 * /api/posts/** GET   → public, max-age=300 (공개 콘텐츠 5분 캐시, 추후 ETag와 조합)
 * 나머지              → no-store (기본 보수적 정책)
 * </pre>
 */
@Component
@Order(1)
public class ResponseHeaderFilter extends OncePerRequestFilter {

    @Value("${app.api-version:1}")
    private String apiVersion;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        chain.doFilter(request, response);

        // 응답 후 헤더 추가 (chain 이후 실행)
        String traceId = MDC.get(MdcKey.TRACE_ID);
        if (traceId != null) {
            response.setHeader(HttpHeader.X_TRACE_ID, traceId);
        }

        response.setHeader(HttpHeader.X_API_VERSION, apiVersion);

        // 클라이언트가 보낸 X-Request-Id 에코 (없으면 생략)
        String requestId = request.getHeader(HttpHeader.X_REQUEST_ID);
        if (requestId != null && !requestId.isBlank()) {
            response.setHeader(HttpHeader.X_REQUEST_ID, requestId);
        }

        // Cache-Control 정책
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/") || uri.equals("/oauth2/token")) {
            response.setHeader(HttpHeader.CACHE_CONTROL, "no-store");
            response.setHeader(HttpHeader.PRAGMA, "no-cache");
        } else if ("GET".equalsIgnoreCase(request.getMethod())
                && uri.startsWith("/api/posts")) {
            // 공개 콘텐츠: 5분 공유 캐시 허용 (ETag로 조건부 요청 추후 구현)
            response.setHeader(HttpHeader.CACHE_CONTROL, "public, max-age=300");
        } else {
            response.setHeader(HttpHeader.CACHE_CONTROL, "no-store");
        }
    }
}
