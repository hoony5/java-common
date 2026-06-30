package com.deoksan.share.security;

/**
 * 감사 로그 / 보안 이벤트 알림 인터페이스.
 *
 * share 패키지가 auth 패키지에 의존하지 않도록 인터페이스를 여기에 두고,
 * SecurityAlertService(auth)가 구현한다. AuditService는 Optional 주입으로
 * 구현체가 없는 서버(blog 등)에서도 정상 동작한다.
 */
public interface AlertService {

    /** 감사 로그 DB 저장 실패 시 알림 */
    void alertAuditFailure(String action, Long userId, String errorMessage);
}
