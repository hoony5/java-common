package com.deoksan.share.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Transactional Outbox 패턴 — 이벤트 저장 엔티티 (F-EVT-1).
 *
 * <p>비즈니스 트랜잭션과 동일한 DB 트랜잭션 안에서 저장되므로
 * 이벤트 유실 없이 at-least-once 전달을 보장한다.
 *
 * <p>상태 흐름: PENDING → PROCESSED (정상) / PENDING → FAILED (오류)
 */
@Entity
@Table(
    name = "outbox_event",
    indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이벤트 타입 식별자 (예: "USER_REGISTERED", "ROLE_CHANGED") */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    /** 이벤트가 발생한 애그리게이트 ID (예: userId) */
    @Column(name = "aggregate_id", length = 255)
    private String aggregateId;

    /** JSON 직렬화된 이벤트 페이로드 */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    /** 처리 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    /** 이벤트 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** 처리 완료 일시 */
    @Column(name = "processed_at")
    private Instant processedAt;

    /** 재시도 횟수 */
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    /** 마지막 오류 메시지 */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** 처리 가능한 상태 목록 */
    public enum Status {
        PENDING,
        PROCESSED,
        FAILED
    }

    @Builder
    private OutboxEvent(String eventType, String aggregateId, String payload) {
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = Status.PENDING;
        this.createdAt = Instant.now();
        this.retryCount = 0;
    }

    /**
     * 이벤트 처리 성공 처리.
     * status = PROCESSED, processedAt = now()
     */
    public void markProcessed() {
        this.status = Status.PROCESSED;
        this.processedAt = Instant.now();
    }

    /**
     * 이벤트 처리 실패 처리.
     * status = FAILED, retryCount 증가, errorMessage 기록
     *
     * @param error 오류 메시지 (최대 길이를 초과해도 DB 트런케이션 방지를 위해 잘라냄)
     */
    public void markFailed(String error) {
        this.status = Status.FAILED;
        this.retryCount++;
        this.errorMessage = error != null && error.length() > 2000
                ? error.substring(0, 2000)
                : error;
    }
}
