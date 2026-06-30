package com.deoksan.share.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Outbox 이벤트 저장소.
 *
 * <p>폴러가 PENDING 이벤트를 배치로 조회할 때 사용한다.
 * partial index (WHERE status = 'PENDING')로 인덱스가 걸려 있어 조회 비용이 낮다.
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * 특정 상태의 이벤트를 created_at ASC 순으로 페이지 단위 조회.
     *
     * @param status   조회 대상 상태 (PENDING)
     * @param pageable 페이지 크기 제한 (예: PageRequest.of(0, 50))
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEvent.Status status, Pageable pageable);
}
