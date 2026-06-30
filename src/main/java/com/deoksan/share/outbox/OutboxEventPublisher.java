package com.deoksan.share.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Transactional Outbox — 이벤트 발행 컴포넌트 (F-EVT-1).
 *
 * <p>비즈니스 서비스 내부의 트랜잭션 컨텍스트 안에서 호출해야 한다.
 * Outbox 레코드를 동일 트랜잭션으로 저장하므로 비즈니스 연산과 이벤트 기록이
 * 원자적으로 커밋/롤백된다.
 *
 * <pre>{@code
 * // 사용 예
 * @Transactional
 * public void changeRole(Long userId, Role newRole) {
 *     AppUser user = userRepository.findById(userId).orElseThrow(...);
 *     user.changeRole(newRole);
 *     outboxEventPublisher.publish("ROLE_CHANGED", userId.toString(),
 *             Map.of("before", oldRole, "after", newRole));
 * }
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * 이벤트를 Outbox 테이블에 저장한다.
     *
     * <p>호출 시점의 활성 트랜잭션과 같은 커넥션을 사용하므로
     * 비즈니스 연산이 롤백되면 이벤트 레코드도 함께 롤백된다.
     *
     * @param eventType   이벤트 타입 식별자 (예: "USER_REGISTERED")
     * @param aggregateId 이벤트 발생 대상 ID (예: userId.toString())
     * @param payload     직렬화할 페이로드 객체 (Map, Record, DTO 등)
     * @throws IllegalStateException payload JSON 직렬화 실패 시
     */
    public void publish(String eventType, String aggregateId, Object payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("[Outbox] payload 직렬화 실패 eventType={} aggregateId={}: {}",
                    eventType, aggregateId, e.getMessage());
            throw new IllegalStateException(
                    "Outbox payload 직렬화 실패: eventType=" + eventType, e);
        }

        OutboxEvent event = OutboxEvent.builder()
                .eventType(eventType)
                .aggregateId(aggregateId)
                .payload(json)
                .build();

        outboxEventRepository.save(event);

        log.debug("[Outbox] 이벤트 저장 완료 eventType={} aggregateId={} id={}",
                eventType, aggregateId, event.getId());
    }
}
