package com.deoksan.share.config;

import com.deoksan.share.web.RequestLoggingInterceptor;
import com.deoksan.share.web.PerformanceInterceptor;
import com.deoksan.share.web.CurrentUserIdArgumentResolver;
import com.deoksan.share.config.TtlConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;

/**
 * Web MVC 설정.
 *
 * CORS 허용 오리진은 AppProperties에서 읽어오므로,
 * application.properties의 "app.cors-allowed-origins" 또는 환경변수로 변경 가능하다.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AppProperties props;
    private final RequestLoggingInterceptor requestLoggingInterceptor;
    private final PerformanceInterceptor performanceInterceptor;
    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;
    private final LocaleChangeInterceptor localeChangeInterceptor;

    public WebMvcConfig(
            AppProperties props,
            RequestLoggingInterceptor requestLoggingInterceptor,
            PerformanceInterceptor performanceInterceptor,
            CurrentUserIdArgumentResolver currentUserIdArgumentResolver,
            LocaleChangeInterceptor localeChangeInterceptor
    ) {
        this.props = props;
        this.requestLoggingInterceptor = requestLoggingInterceptor;
        this.performanceInterceptor = performanceInterceptor;
        this.currentUserIdArgumentResolver = currentUserIdArgumentResolver;
        this.localeChangeInterceptor = localeChangeInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = props.getCorsAllowedOrigins().toArray(new String[0]);

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                // 허용 요청 헤더 — * 대신 명시 (ISO 27002 A.8.3)
                .allowedHeaders(
                        "Authorization",
                        "Content-Type",
                        "X-Refresh-Token",
                        "X-Client-Channel",    // WEB|APP 채널 구분
                        "X-Correlation-Id",    // 분산 추적 흐름 ID
                        "X-Request-Id",        // 클라이언트 요청 ID (idempotency)
                        "X-Api-Key",           // MCP API 키
                        "X-Client-Version",    // 클라이언트 앱 버전
                        "Accept-Language"
                )
                // 노출 응답 헤더 — JS(fetch/axios)에서 접근 가능하도록
                .exposedHeaders(
                        "X-Trace-Id",          // 서버 추적 ID (오류 리포트용)
                        "X-Api-Version",       // API 버전
                        "X-Request-Id",        // 에코된 요청 ID
                        "Retry-After",         // 429 재시도 대기 시간
                        "WWW-Authenticate"     // 401 인증 방식 안내
                )
                .allowCredentials(true)
                .maxAge(TtlConstants.CORS_PREFLIGHT_SECONDS);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Boot 자동 설정이 /static에서 정적 리소스를 제공한다.
        // 커스텀 리소스 경로가 필요할 때만 여기서 재정의한다.
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 언어 변경 인터셉터 (가장 먼저 실행)
        registry.addInterceptor(localeChangeInterceptor)
                .addPathPatterns("/api/**")
                .order(0);

        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/api/**")
                .order(1);

        registry.addInterceptor(performanceInterceptor)
                .addPathPatterns("/api/**")
                .order(2);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdArgumentResolver);
    }
}
