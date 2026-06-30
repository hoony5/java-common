package com.deoksan.share.web;

/**
 * 이 프로젝트에서 사용하는 HTTP 헤더 이름 상수.
 *
 * Spring의 HttpHeaders는 표준 헤더를 다루고,
 * 이 클래스는 커스텀 X-* 헤더와 자주 쓰는 표준 헤더를 한곳에 모은다.
 * 어노테이션(@RequestHeader, @RequestMapping 등)의 문자열 속성에도
 * 컴파일 타임 상수로 직접 사용 가능하다.
 */
public final class HttpHeader {

    // ─── Standard ────────────────────────────────────────────────────────────
    public static final String AUTHORIZATION    = "Authorization";
    public static final String USER_AGENT       = "User-Agent";
    public static final String SET_COOKIE       = "Set-Cookie";
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String RETRY_AFTER      = "Retry-After";
    public static final String CACHE_CONTROL    = "Cache-Control";
    public static final String PRAGMA           = "Pragma";

    // ─── Custom — request ────────────────────────────────────────────────────
    /** MCP API 키. 우선순위: X-Api-Key > Authorization: ApiKey */
    public static final String X_API_KEY        = "X-Api-Key";

    /** WEB 채널의 리프레시 토큰 전달 헤더 */
    public static final String X_REFRESH_TOKEN  = "X-Refresh-Token";

    /** 토큰 전달 채널 구분 (WEB | APP). 기본값: APP */
    public static final String X_CLIENT_CHANNEL = "X-Client-Channel";

    /** 비즈니스 흐름 추적 ID. 없으면 traceId로 대체 */
    public static final String X_CORRELATION_ID = "X-Correlation-Id";

    /** 클라이언트 요청 ID — 응답에 에코 (idempotency 확인) */
    public static final String X_REQUEST_ID     = "X-Request-Id";

    /** 클라이언트 앱 버전 (로깅/분기용) */
    public static final String X_CLIENT_VERSION = "X-Client-Version";

    /** 리버스 프록시가 전달하는 실제 클라이언트 IP */
    public static final String X_FORWARDED_FOR  = "X-Forwarded-For";

    // ─── Cloudflare ──────────────────────────────────────────────────────────
    /**
     * Cloudflare가 설정하는 실제 클라이언트 IP 헤더.
     * X-Forwarded-For보다 신뢰도가 높다 — Cloudflare 엣지에서 덮어쓰므로
     * 클라이언트가 직접 위조할 수 없다 (Cloudflare를 통과해야 생성됨).
     * IpUtils에서 이 헤더를 최우선으로 사용한다.
     */
    public static final String CF_CONNECTING_IP = "CF-Connecting-IP";

    /**
     * Cloudflare가 설정하는 요청 국가 코드 (ISO 3166-1 alpha-2).
     * 지역 차단, 로깅 등에 활용 가능.
     */
    public static final String CF_IPCOUNTRY     = "CF-IPCountry";

    /** Cloudflare Ray ID — 엣지 요청 추적 (Cloudflare 대시보드와 연결) */
    public static final String CF_RAY           = "CF-Ray";

    // ─── Custom — response ───────────────────────────────────────────────────
    /** 서버 요청 추적 ID — 오류 리포트 시 제출 */
    public static final String X_TRACE_ID       = "X-Trace-Id";

    /** API 버전 — app.api-version 설정값 */
    public static final String X_API_VERSION    = "X-Api-Version";

    private HttpHeader() {}
}
