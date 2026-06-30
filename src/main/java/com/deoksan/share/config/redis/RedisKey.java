package com.deoksan.share.config.redis;

import java.time.Duration;

/**
 * Redis 키.prefix 및 기본 TTL을 타입 안전하게 관리하는 열거형.
 *
 * <h3>사용 예</h3>
 * <pre>
 * String key = RedisKey.SESSION.of(userId);
 * Duration ttl = RedisKey.SESSION.getDefaultTtl();
 * </pre>
 *
 * <h3>키 네이밍 규칙</h3>
 * <ul>
 *   <li>모든 키는 {@code {project}:{domain}:} 접두사를 따른다</li>
 *   <li>Redis CLI에서 패턴 검색 시 {@code blog:*}으로 전체 조회 가능</li>
 * </ul>
 */
public enum RedisKey {

    /**
     * 속도 제한 — IP별 로그인 시도 횟수 카운터.
     * <p>Key: {@code blog:limit:{ip}}
     * <p>Type: STRING (counter)
     */
    RATE_LIMIT("blog:limit:", Duration.ofMinutes(15)),

    /**
     * 계정 잠금 — 임시 잠금 플래그.
     * <p>Key: {@code blog:lock:{userId}}
     * <p>Type: STRING ("locked")
     * <p>TTL: 호출 시 동적 설정 (기본값 없음)
     */
    LOCK("blog:lock:", null),

    /**
     * OAuth2 일회성 인증 코드 — 토큰 교환용.
     *
     * [보안 개선] OAuth2 콜백 시 토큰을 URL에 직접 포함하면 브라우저 히스토리·
     * 서버 액세스 로그·Referer 헤더에 토큰이 노출된다.
     * 대신 60초 TTL의 일회성 코드를 Redis에 저장하고, 코드만 URL에 포함한다.
     * 프론트엔드는 코드를 받아 POST /api/auth/exchange를 호출해 실제 토큰을 얻는다.
     * getAndDelete로 조회 시 즉시 삭제 → 재사용 불가.
     *
     * <p>Key: {@code blog:auth:code:{uuid}}
     * <p>Value: "userId|accessToken|refreshToken|expiresAt|autoLoginKey" (pipe 구분)
     * <p>TTL: 60초
     */
    AUTH_CODE("blog:auth:code:", Duration.ofSeconds(60)),

    /**
     * 사용자 세션 무효화 기준 시각.
     *
     * 로그아웃/강제 로그아웃 시 현재 시각(epoch seconds)을 저장한다.
     * JWT 인증 필터는 토큰의 iat(issued-at)가 이 값보다 이전이면 토큰을 거부한다.
     * → 이 시각 이전에 발급된 모든 JWT가 즉시 무효화된다.
     *
     * <p>Key: {@code blog:user:invalid-since:{userId}}
     * <p>Value: epoch seconds (String)
     * <p>TTL: 30일 (refresh token 최대 수명과 동일)
     */
    INVALID_SINCE("blog:user:invalid-since:", Duration.ofDays(30)),

    /**
     * API 엔드포인트별 속도 제한 — {@code @RateLimit} AOP 사용.
     *
     * <p>Key: {@code blog:limit:rl:{methodName}:{ip}}
     * <p>Type: STRING (counter)
     * <p>TTL: 어노테이션의 per/unit으로 동적 설정 (defaultTtl 미사용)
     */
    RATE_LIMIT_API("blog:limit:rl:", null),

    /**
     * MFA 이메일 OTP — ROLE_MASTER 이중 인증용.
     *
     * <p>Key: {@code blog:mfa:otp:{userId}}
     * <p>Value: 6자리 숫자 OTP
     * <p>TTL: 5분 (검증 실패 포함 3회 초과 시 재발급 필요)
     */
    MFA_OTP("blog:mfa:otp:", Duration.ofMinutes(5)),

    /**
     * MFA 보류 인증 데이터 — MFA 검증 완료 전까지 토큰 발급 대기.
     *
     * <p>Key: {@code blog:mfa:pending:{pendingToken}}
     * <p>Value: "userId|accessToken|refreshToken|expiresAt|autoLoginKey" (AUTH_CODE와 동일 포맷)
     * <p>TTL: 5분
     */
    MFA_PENDING("blog:mfa:pending:", Duration.ofMinutes(5)),

    /**
     * Passkey (WebAuthn) 챌린지 — 등록/인증 ceremony 중간 상태.
     *
     * <p>Key: {@code blog:passkey:challenge:reg:{userId}} (등록)
     * <p>Key: {@code blog:passkey:challenge:auth:{challengeId}} (인증)
     * <p>Value: PublicKeyCredentialCreationOptions / AssertionRequest JSON
     * <p>TTL: 5분
     */
    PASSKEY_CHALLENGE("blog:passkey:challenge:", Duration.ofMinutes(5)),

    /**
     * IP 블랙리스트 — 특정 IP의 로그인/요청 차단.
     * <p>Key: {@code blog:blacklist:ip:{ip}}
     * <p>Value: 차단 사유 (String)
     * <p>TTL: 기본 7일 (관리자가 더 짧게/길게 지정 가능)
     */
    IP_BLACKLIST("blog:blacklist:ip:", Duration.ofDays(7)),

    /**
     * 이메일 블랙리스트 — 탈퇴/차단된 이메일의 재가입 방지.
     * <p>Key: {@code blog:blacklist:email:{email}}
     * <p>Value: 차단 사유 (String)
     * <p>TTL: null = 영구 (관리자가 직접 삭제)
     */
    EMAIL_BLACKLIST("blog:blacklist:email:", null);

    private final String prefix;
    private final Duration defaultTtl;

    RedisKey(String prefix, Duration defaultTtl) {
        this.prefix = prefix;
        this.defaultTtl = defaultTtl;
    }

    /**
     * 키를 조합하여 전체 Redis 키를 생성한다.
     *
     * @param parts 키 뒤에 붙일 식별자들 (toString() 호출됨)
     * @return 완성된 Redis 키
     */
    public String of(Object... parts) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object p : parts) {
            sb.append(p);
        }
        return sb.toString();
    }

    /**
     * 이 키 유형의 기본 TTL을 반환한다.
     *
     * @return 기본 TTL — null인 경우 호출자가 동적으로 TTL 설정
     */
    public Duration getDefaultTtl() {
        return defaultTtl;
    }

    /**
     * TTL이 반드시 필요한 경우 사용. null이면 IllegalStateException.
     * TTL이 선택적인 경우는 {@link #getDefaultTtl()} + null 체크를 직접 사용한다.
     */
    public Duration getRequiredTtl() {
        if (defaultTtl == null) {
            throw new IllegalStateException(
                "RedisKey." + name() + "의 기본 TTL이 없습니다. 호출 시 TTL을 직접 지정하세요.");
        }
        return defaultTtl;
    }

    public boolean hasTtl() {
        return defaultTtl != null;
    }

    /**
     * 키 접두사만 반환한다 (디버깅/모니터링용).
     */
    public String getPrefix() {
        return prefix;
    }
}
