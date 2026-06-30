package com.deoksan.share.event;

import com.deoksan.share.config.logging.MdcKey;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.UUID;

/**
 * 도메인 이벤트 기반 클래스.
 *
 * 모든 도메인 이벤트는 이 클래스를 상속한다.
 * traceId / correlationId를 발행 시점의 MDC에서 캡처하여 이벤트에 고정한다.
 *
 * [분산 환경 전환 시]
 * Kafka 메시지로 전환하면 이 필드들을 메시지 헤더에 실어 분산 추적 연속성을 유지한다.
 * eventId는 멱등성 보장을 위한 메시지 ID로 재활용 가능하다.
 */
public abstract class DomainEvent {

    private final String eventId;
    private final String traceId;
    private final String correlationId;
    private final Instant occurredAt;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString().replace("-", "");
        this.traceId = toNaIfNull(MDC.get(MdcKey.TRACE_ID));
        this.correlationId = toNaIfNull(MDC.get(MdcKey.CORRELATION_ID));
        this.occurredAt = Instant.now();
    }

    private static String toNaIfNull(String value) {
        return value != null ? value : "N/A";
    }

    public String getEventId()       { return eventId; }
    public String getTraceId()       { return traceId; }
    public String getCorrelationId() { return correlationId; }
    public Instant getOccurredAt()   { return occurredAt; }
}
