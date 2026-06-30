package com.deoksan.share.outbox;

import org.springframework.context.ApplicationEvent;

/**
 * Outbox 폴러가 PENDING 이벤트를 처리할 때 발행하는 Spring ApplicationEvent.
 *
 * <p>동일 JVM 내의 {@code @EventListener} 또는
 * {@code @TransactionalEventListener} 빈이 이 이벤트를 수신할 수 있다.
 * F-EVT-2에서 외부 메시지 브로커로 교체될 때까지 내부 전파 채널로 사용한다.
 */
public class OutboxEventPublishedEvent extends ApplicationEvent {

    private final OutboxEvent outboxEvent;

    public OutboxEventPublishedEvent(Object source, OutboxEvent outboxEvent) {
        super(source);
        this.outboxEvent = outboxEvent;
    }

    /** 처리 중인 원본 Outbox 이벤트 */
    public OutboxEvent getOutboxEvent() {
        return outboxEvent;
    }
}
