package com.deoksan.share.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * 지수 백오프 재시도 설정.
 *
 * @Retryable 적용 대상은 각 서비스에서 직접 선언한다.
 *
 * 기본 정책 (각 @Retryable에서 오버라이드 가능):
 *  시도 1 → 즉시
 *  시도 2 → 1000ms 후
 *  시도 3 → 2000ms 후  (multiplier=2)
 *  최대 대기: ~3s → 사용자 UX 허용 범위 내
 */
@EnableRetry
@Configuration
public class RetryConfig {
}
