package com.deoksan.share.bootstrap;

/**
 * 도메인별 초기화 클래스가 구현하는 인터페이스.
 *
 * <p>AppBootstrapper가 이 인터페이스를 구현한 모든 빈을 수집하여
 * {@link #order()} 값이 작은 순서대로 차례대로 실행한다.
 *
 * <h3>order()만 분리한 이유</h3>
 * <p>두 메서드가 있는 이유:
 * <ul>
 *   <li>{@code order()} — 실행 순서 결정. 초기화 파이프라인에서
 *       어떤 도메인이 먼저 준비되어야 하는지 명시한다.
 *       (예: DB → 보안 → 캐시 → 블로그 순)</li>
 *   <li>{@code initialize()} — 실제 초기화 로직.
 *       각 도메인이 스스로를 준비시키는 코드를 작성한다.</li>
 * </ul>
 *
 * <p>왜 인터페이스인가? — 구현체가 어떤 클래스를 상속하든
 * bootstrap 파이프라인에 참여할 수 있도록 하기 위함이다.
 * 예를 들어 JpaConfig를 상속받거나, 다른 추상 클래스를 확장해도
 * DomainBootstrap만 구현하면 AppBootstrapper가 자동으로 발견한다.
 *
 * <h3>예약된 order 범위</h3>
 * <pre>
 *   10-19   인프라 (DB 스키마, Flyway)
 *   20-29   보안 (역할, OAuth2 클라이언트 검증)
 *   30-39   캐시 (Redis 웜업)
 *   40-49   인증 도메인 초기화
 *   50-59   블로그 도메인 초기화
 *   100+    초기화 완료 후 점검
 * </pre>
 */
public interface DomainBootstrap {

    /** 초기화 실행 순서. 숫자가 작을수록 먼저 실행된다. */
    int order();

    /** 실제 초기화 로직. */
    void initialize();
}
