package com.deoksan.share.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 캐시 매니저 초기화.
 *
 * <p>보안 다음으로 실행된다 (order = 30).
 * 세션 데이터 캐싱이 보안 컨텍스트에 의존하므로 순서가 중요하다.
 */
@Slf4j
@Component
public class CacheBootstrap implements DomainBootstrap {

    public static final int ORDER = 30;

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public void initialize() {
        log.info("[CacheBootstrap] Redis cache manager ready.");
    }
}
