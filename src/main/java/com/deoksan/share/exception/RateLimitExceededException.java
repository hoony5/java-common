package com.deoksan.share.exception;

/**
 * API 호출 빈도 제한 초과 예외.
 *
 * [이전 문제] RateLimitAspect가 IllegalArgumentException을 던졌고,
 * GlobalExceptionHandler가 이를 400 Bad Request로 처리했음.
 * Rate limit 초과는 HTTP 429 Too Many Requests가 표준(RFC 6585)이다.
 *
 * [해결] 전용 예외를 만들고 GlobalExceptionHandler에서 429로 매핑.
 * RuntimeException을 상속하여 Spring이 별도 catch 처리 없이도 전파됨.
 *
 * [학습 포인트] 커스텀 예외 계층 설계
 *   예외 타입이 구체적일수록 처리 전략을 세분화할 수 있다.
 *   IllegalArgumentException처럼 범용 예외를 쓰면 rate limit 초과와
 *   잘못된 인자를 구분할 수 없어 응답 코드가 동일해진다.
 */
public class RateLimitExceededException extends RuntimeException {

    private final String limitKey;
    private final long currentCount;
    private final long maxCount;
    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, String limitKey,
                                       long currentCount, long maxCount,
                                       long retryAfterSeconds) {
        super(message);
        this.limitKey = limitKey;
        this.currentCount = currentCount;
        this.maxCount = maxCount;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getLimitKey() { return limitKey; }
    public long getCurrentCount() { return currentCount; }
    public long getMaxCount() { return maxCount; }
    public long getRetryAfterSeconds() { return retryAfterSeconds; }
}
