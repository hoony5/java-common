package com.deoksan.share.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 호출 시 감사(audit) 로그를 자동으로 기록한다.
 *
 * 누가, 언제, 무엇을 했는지 추적할 수 있다.
 * MDC에 액션 정보가 포함되어 로그에서 추적 가능하다.
 *
 * 사용 예:
 * <pre>
 * {@code @AuditLog(action = "POST_DELETE", description = "포스트 삭제")}
 * {@code public void deletePost(Long id) { ... }}
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /** 감사 로그에 기록할 액션 식별자 */
    String action();

    /** 사람이 읽을 수 있는 액션 설명 (선택) */
    String description() default "";
}
