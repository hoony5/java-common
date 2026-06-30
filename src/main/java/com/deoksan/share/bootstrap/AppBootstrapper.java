package com.deoksan.share.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 도메인 초기화 파이프라인 코디네이터.
 *
 * <p>Spring이 {@link DomainBootstrap}을 구현한 모든 빈을 자동으로 수집하여
 * {@link #order()} 값 기준으로 정렬한 뒤 순차적으로 실행한다.
 *
 * <h3>자동 발견 원리</h3>
 * <p>Spring의 {@code List<T>} 주입 기능을 사용한다.
 * 새로운 도메인 부트스트랩을 추가하려면:
 * <ol>
 *   <li>{@code @Component} 어노테이션 추가</li>
 *   <li>{@link DomainBootstrap} 인터페이스 구현</li>
 * </ol>
 * 이 두 가지만 만족하면 AppBootstrapper가 자동으로 감지하여 실행한다.
 * 별도의 등록 코드는 필요 없다.
 *
 * <h3>실행 시점</h3>
 * <p>{@link ApplicationRunner}이므로 Spring 컨텍스트 초기화 완료 후,
 * 애플리케이션이 요청을 받기 직전에 실행된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppBootstrapper implements ApplicationRunner {

    private final List<DomainBootstrap> bootstraps;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== [Bootstrap] Starting domain initialization pipeline ===");

        bootstraps.stream()
                .sorted(Comparator.comparingInt(DomainBootstrap::order))
                .forEach(b -> {
                    log.info("[Bootstrap] Running: {} (order={})",
                            b.getClass().getSimpleName(), b.order());
                    b.initialize();
                });

        log.info("=== [Bootstrap] Domain initialization pipeline complete ===");
    }
}
