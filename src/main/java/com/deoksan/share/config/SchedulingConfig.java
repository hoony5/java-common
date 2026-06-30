package com.deoksan.share.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 스케줄링 + 비동기 실행 설정.
 *
 * @EnableAsync: @Async 메서드가 별도 스레드 풀에서 실행되도록 활성화.
 * 이벤트 리스너에서 @Async를 사용하면 메인 요청 스레드를 블록하지 않는다.
 * MDC는 ThreadLocal이므로 Async 메서드에서는 DomainEvent에 담긴 ID를 직접 설정해야 함.
 *
 * [학습 포인트] @EnableScheduling
 *   이 어노테이션 없이 @Scheduled를 붙여도 동작하지 않는다.
 *   Spring이 스케줄러 스레드 풀을 초기화하고 @Scheduled 메서드를 탐색하는
 *   백그라운드 태스크 실행기를 등록하는 역할이다.
 *
 * [왜 별도 Configuration인가?]
 *   메인 애플리케이션 클래스(@SpringBootApplication)에 붙여도 되지만,
 *   설정 클래스로 분리하면 테스트 시 @Import 없이 스케줄링을 끌 수 있고,
 *   나중에 TaskExecutor 커스터마이징(스레드 수 등)을 여기서만 수정하면 된다.
 *
 * [Render 무료 티어 주의]
 *   Render 무료 인스턴스는 15분 비활성 시 슬립 상태가 된다.
 *   슬립 중에는 스케줄러도 동작하지 않는다.
 *   유료 플랜 또는 Render Cron Job(별도 서비스)을 사용하는 것이 안전하다.
 *   토큰 정리, 탈퇴 처리 등 중요한 작업은 누락을 허용할 수 있는 best-effort 방식으로 설계함.
 */
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {

    /**
     * 이벤트 리스너 전용 스레드 풀.
     * 로그인 이력 기록 등 메인 흐름과 무관한 처리를 비동기로 실행한다.
     * "event-" 접두사로 스레드를 식별하면 스레드 덤프에서 이벤트 처리 현황 파악 가능.
     */
    private static final int EVENT_POOL_CORE_SIZE  = 2;
    private static final int EVENT_POOL_MAX_SIZE   = 4;
    private static final int EVENT_QUEUE_CAPACITY  = 50;

    @Bean(name = "eventTaskExecutor")
    public TaskExecutor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(EVENT_POOL_CORE_SIZE);
        executor.setMaxPoolSize(EVENT_POOL_MAX_SIZE);
        executor.setQueueCapacity(EVENT_QUEUE_CAPACITY);
        executor.setThreadNamePrefix("event-");
        executor.initialize();
        return executor;
    }
}
