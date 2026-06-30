package com.deoksan.share.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 데이터베이스 초기화.
 *
 * <p>가장 먼저 실행된다 (order = 10).
 * Flyway가 스키마 마이그레이션을 완료한 후 호출되며,
 * 다른 모든 부트스트랩이 DB 접근을 필요로 하므로 순서가 중요하다.
 */
@Slf4j
@Component
public class DatabaseBootstrap implements DomainBootstrap {

    public static final int ORDER = 10;

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public void initialize() {
        log.info("[DatabaseBootstrap] Flyway managed schema is ready.");
    }
}
