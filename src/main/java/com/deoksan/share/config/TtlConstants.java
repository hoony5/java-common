package com.deoksan.share.config;

import java.time.Duration;

/**
 * 프로젝트 전역 TTL / 유효 기간 상수.
 *
 * Redis TTL은 {@link com.deoksan.share.config.redis.RedisKey}에 있다.
 * 이 클래스는 Redis 외의 시간값(쿠키, CORS, HSTS, 스프링 캐시 기본값)을 담는다.
 *
 * <h3>변경 원칙</h3>
 * <ul>
 *   <li>보안 정책값(HSTS)은 상수로 고정 — 실수로 짧게 줄이는 것을 방지</li>
 *   <li>운영 중 변경이 필요한 값은 application.properties의 @Value로 주입</li>
 * </ul>
 */
public final class TtlConstants {

    private TtlConstants() {}

    // ─── 쿠키 ─────────────────────────────────────────────────────────────────

    /** WEB 채널 refresh token HttpOnly 쿠키 유효 기간 */
    public static final Duration REFRESH_COOKIE = Duration.ofDays(30);

    /** i18n(언어 설정) 쿠키 유효 기간 */
    public static final Duration LOCALE_COOKIE = Duration.ofDays(30);

    // ─── CORS ────────────────────────────────────────────────────────────────

    /** CORS preflight 캐시 (브라우저가 OPTIONS 요청을 재사용하는 시간) */
    public static final long CORS_PREFLIGHT_SECONDS = 3_600L;    // 1시간

    // ─── 보안 헤더 ───────────────────────────────────────────────────────────

    /** HSTS max-age: 1년 (RFC 6797 권장값) */
    public static final long HSTS_MAX_AGE_SECONDS = 31_536_000L;

    // ─── Spring Cache (Redis-backed @Cacheable) ───────────────────────────────

    /** @Cacheable 기본 TTL — 잦은 변경 없는 참조 데이터용 */
    public static final Duration DEFAULT_CACHE = Duration.ofMinutes(30);
}
