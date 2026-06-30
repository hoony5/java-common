package com.deoksan.share.aspect;

import com.deoksan.share.annotation.RateLimit;
import com.deoksan.share.config.redis.RedisKey;
import com.deoksan.share.exception.RateLimitExceededException;
import com.deoksan.share.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.UUID;

/**
 * {@link RateLimit} 어노테이션이 붙은 메서드의 호출 빈도를 제한한다.
 *
 * <p>Redis Sorted Set 기반 슬라이딩 윈도우 알고리즘을 사용한다.
 * 고정 윈도우(Fixed Window)와 달리 윈도우 경계에서의 버스트 요청 문제가 없다.
 *
 * <p>동작 방식:
 * <ol>
 *   <li>ZADD: 현재 요청을 타임스탬프(ms)를 스코어로 Sorted Set에 추가</li>
 *   <li>ZREMRANGEBYSCORE: 윈도우 밖의 오래된 항목 제거</li>
 *   <li>ZCARD: 윈도우 내 요청 수 조회</li>
 *   <li>EXPIRE: TTL 갱신 (윈도우 크기 + 1초 여유)</li>
 * </ol>
 *
 * 키 패턴: {@code blog:limit:rl:{메서드명}:{요청자IP}}
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object limit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String ip = resolveClientIp();
        String methodName = joinPoint.getSignature().toShortString()
                .replaceAll("[^a-zA-Z0-9]", "_");
        String key = RedisKey.RATE_LIMIT_API.of(methodName + ":" + ip);

        Duration ttl = Duration.of(rateLimit.per(), rateLimit.unit().toChronoUnit());
        long windowMs = ttl.toMillis();
        long currentTimeMs = System.currentTimeMillis();
        String member = currentTimeMs + "-" + UUID.randomUUID();

        // 슬라이딩 윈도우: 현재 요청 추가 (score = 타임스탬프 ms)
        redisTemplate.opsForZSet().add(key, member, currentTimeMs);
        // 윈도우 범위 바깥의 오래된 항목 제거
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, currentTimeMs - windowMs);
        // 윈도우 내 요청 수 조회
        Long count = redisTemplate.opsForZSet().size(key);
        // TTL 갱신: 윈도우 크기 + 1초 여유 (빈 키가 영구 잔존하지 않도록)
        redisTemplate.expire(key, Duration.ofSeconds(ttl.toSeconds() + 1));

        if (count != null && count > rateLimit.max()) {
            log.warn("[RATELIMIT] {} 호출 제한 초과 ({}/{}) from {}", methodName, count, rateLimit.max(), ip);
            long retryAfter = rateLimit.retryAfterSeconds() > 0
                    ? rateLimit.retryAfterSeconds()
                    : ttl.getSeconds();
            throw new RateLimitExceededException(rateLimit.message(), key, count, rateLimit.max(), retryAfter);
        }

        log.debug("[RATELIMIT] {} 호출 ({}/{}) from {}", methodName, count, rateLimit.max(), ip);
        return joinPoint.proceed();
    }

    private String resolveClientIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        return IpUtils.resolveClientIp(attrs.getRequest());
    }
}
