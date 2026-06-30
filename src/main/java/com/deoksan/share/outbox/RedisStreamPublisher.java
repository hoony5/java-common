package com.deoksan.share.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Redis Streams 이벤트 발행기 (F-EVT-2).
 *
 * <p>{@link OutboxPollerScheduler}가 PENDING 이벤트를 처리하면서
 * {@link OutboxEventPublishedEvent}를 발행하면, 이 빈이 이를 수신하여
 * Redis Stream에 기록한다.
 *
 * <p>스트림 키 형식: {@code blog:events:{eventType}}
 * (예: USER_REGISTERED → {@code blog:events:user.registered})
 *
 * <p>{@code app.outbox.enabled=true} 로 설정해야 활성화된다(기본값: 비활성).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.outbox.enabled", havingValue = "true", matchIfMissing = false)
public class RedisStreamPublisher {

    private final StringRedisTemplate redisTemplate;

    /**
     * Outbox 이벤트를 Redis Stream에 발행한다.
     *
     * <p>스트림 키는 이벤트 타입을 소문자로 변환하고 '_'를 '.'으로 치환하여 결정한다.
     * 예: {@code USER_REGISTERED} → {@code blog:events:user.registered}
     *
     * @param event Spring ApplicationEvent로 전달된 Outbox 이벤트
     */
    @EventListener
    public void onOutboxEvent(OutboxEventPublishedEvent event) {
        OutboxEvent outboxEvent = event.getOutboxEvent();

        // Stream key: blog:events:{eventType}
        String streamKey = "blog:events:" + outboxEvent.getEventType().toLowerCase().replace("_", ".");

        Map<String, String> fields = Map.of(
                "eventId",     String.valueOf(outboxEvent.getId()),
                "eventType",   outboxEvent.getEventType(),
                "aggregateId", outboxEvent.getAggregateId() != null ? outboxEvent.getAggregateId() : "",
                "payload",     outboxEvent.getPayload(),
                "createdAt",   outboxEvent.getCreatedAt().toString()
        );

        redisTemplate.opsForStream().add(streamKey, fields);

        log.debug("[RedisStream] 이벤트 발행 key={} eventId={} eventType={}",
                streamKey, outboxEvent.getId(), outboxEvent.getEventType());
    }
}
