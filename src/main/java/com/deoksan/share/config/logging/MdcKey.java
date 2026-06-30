package com.deoksan.share.config.logging;

/**
 * MDC(Mapped Diagnostic Context) 키 상수 정의.
 * logback 패턴 및 MdcLoggingFilter, Interceptor 에서 함께 사용.
 */
public final class MdcKey {

    /**
     * 단일 HTTP 요청 추적 ID. 요청 진입 시 생성, 응답 후 제거.
     * 로그에서 하나의 요청 흐름을 통째로 필터링할 때 사용.
     */
    public static final String TRACE_ID = "traceId";

    /**
     * 여러 요청을 묶는 비즈니스 흐름 ID.
     * 클라이언트가 X-Correlation-Id 헤더로 전달하거나, 없으면 traceId로 대체.
     * 예: 매직링크 요청 → 클릭 → 코드 교환 → 토큰 발급을 하나의 흐름으로 묶음.
     * 서비스 분리 후에는 메시지 큐 헤더로 전파되어 분산 추적 연속성 유지.
     */
    public static final String CORRELATION_ID = "correlationId";

    /**
     * 발행된 도메인 이벤트 자체의 UUID.
     * 이벤트 리스너 실행 시 MDC에 설정되어 이벤트 처리 로그와 원본 요청 로그를 연결.
     * Kafka 등으로 전환 시 메시지 ID로 재활용.
     */
    public static final String EVENT_ID = "eventId";

    /**
     * 인증된 사용자 ID. TokenAuthenticationFilter 성공 후 설정.
     */
    public static final String USER_ID = "userId";

    public static final String REQUEST_URI = "requestUri";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String AUDIT_ACTION = "auditAction";

    /**
     * Cloudflare 엣지 요청 식별자.
     * CF 대시보드의 Ray ID와 앱 로그를 직접 연결하는 데 사용.
     * 없으면 "-" (Cloudflare 앞단이 아닌 직접 요청).
     */
    public static final String CF_RAY = "cfRay";

    private MdcKey() {
    }
}
