package com.deoksan.share.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * API 호출 빈도를 제한한다.
 *
 * Redis를 기반으로 하여 분산 환경에서도 정상 동작한다.
 *
 * 사용 예:
 * <pre>
 * // 1분에 최대 60회 호출 허용
 * {@code @RateLimit(max = 60, per = 1, unit = TimeUnit.MINUTES)}
 * {@code @GetMapping("/posts")}
 * public PageResponse<PostDto> list() { ... }
 *
 * // 로그인 시도는 5분에 최대 5회
 * {@code @RateLimit(max = 5, per = 5, unit = TimeUnit.MINUTES)}
 * {@code @PostMapping("/login")}
 * public void login(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 최대 허용 호출 횟수 */
    int max();

    /** 시간 윈도우 길이 */
    int per() default 1;

    /** 시간 단위 */
    TimeUnit unit() default TimeUnit.MINUTES;

    /** 초과 시 응답 메시지 */
    String message() default "요청이 너무 많습니다. 잠시 후 다시 시도하세요.";

    /** Retry-After 헤더 값(초) — 0이면 per×unit으로 자동 계산 */
    int retryAfterSeconds() default 0;
}
