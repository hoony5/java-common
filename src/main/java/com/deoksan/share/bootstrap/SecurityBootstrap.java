package com.deoksan.share.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 보안 컨텍스트 초기화.
 *
 * <p>데이터베이스 다음으로 실행된다 (order = 20).
 * 보안 설정이 DB 기반 UserDetailsService나
 * OAuth2 클라이언트 등록 정보를 필요로 할 수 있다.
 */
@Slf4j
@Component
public class SecurityBootstrap implements DomainBootstrap {

    public static final int ORDER = 20;

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public void initialize() {
        log.info("[SecurityBootstrap] Security context ready.");
    }
}
