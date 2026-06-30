package com.deoksan.share.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Transactional Outbox — 폴러 스케줄러 (F-EVT-1).
 *
 * <p>5초마다 PENDING 상태의 Outbox 이벤트를 배치로 읽어 처리한다.
 * 각 배치는 독립된 트랜잭션으로 실행되어 처리 결과가 원자적으로 커밋된다.
 *
 * <p>현재 구현(F-EVT-1)은 로깅 + Spring ApplicationEvent 재발행만 수행한다.
 * 실제 외부 스트리밍(Kafka 등)은 F-EVT-2에서 추가 예정.
 *
 * <p>{@code app.outbox.enabled=true} 로 설정해야 활성화된다(기본값: 비활성).
 *
 * <h3>fixedDelay vs fixedRate</h3>
 * <ul>
 *   <li>fixedDelay: 이전 실행 완료 후 N ms 대기 → 처리가 길어져도 중첩 없음 (선택)</li>
 *   <li>fixedRate: N ms마다 실행 시작 → 처리 시간 &gt; 인터벌이면 중첩 가능</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.outbox.enabled", havingValue = "true", matchIfMissing = false)
public class OutboxPollerScheduler {

    private static final int BATCH_SIZE = 50;

    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * PENDING 이벤트를 배치로 처리한다.
     *
     * <p>각 호출은 {@code @Transactional}로 감싸져 있어 배치 전체가
     * 하나의 트랜잭션으로 커밋/롤백된다.
     * 단일 이벤트 처리 실패 시 해당 이벤트만 FAILED로 표시하고 계속 진행한다.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void pollAndProcess() {
        List<OutboxEvent> pending = outboxEventRepository
                .findByStatusOrderByCreatedAtAsc(
                        OutboxEvent.Status.PENDING,
                        PageRequest.of(0, BATCH_SIZE));

        if (pending.isEmpty()) {
            return;
        }

        log.info("[Outbox] 폴링 시작 — PENDING 이벤트 {}건", pending.size());

        for (OutboxEvent event : pending) {
            try {
                process(event);
                event.markProcessed();
                log.info("[Outbox] 처리 완료 id={} eventType={} aggregateId={}",
                        event.getId(), event.getEventType(), event.getAggregateId());
            } catch (Exception e) {
                log.error("[Outbox] 처리 실패 id={} eventType={} aggregateId={}: {}",
                        event.getId(), event.getEventType(), event.getAggregateId(),
                        e.getMessage(), e);
                event.markFailed(e.getMessage());
            }
        }

        outboxEventRepository.saveAll(pending);
        log.info("[Outbox] 배치 처리 완료 — {}건 처리", pending.size());
    }

    /**
     * 개별 이벤트 처리 로직.
     *
     * <p>F-EVT-1: 이벤트를 로그로 기록하고 Spring ApplicationEvent로 재발행한다.
     * F-EVT-2에서 외부 메시지 브로커(Kafka 등) 발행으로 교체 예정.
     *
     * @param event 처리할 Outbox 이벤트
     */
    private void process(OutboxEvent event) {
        log.debug("[Outbox] 이벤트 재발행 id={} eventType={} payload={}",
                event.getId(), event.getEventType(), event.getPayload());

        // Spring ApplicationEvent로 재발행 — 동일 JVM 내 리스너가 수신 가능
        applicationEventPublisher.publishEvent(
                new OutboxEventPublishedEvent(this, event));
    }
}
