package com.deoksan.share.annotation;

import com.deoksan.share.security.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 호출에 필요한 최소 역할(Role)을 지정한다.
 *
 * 역할은 계층적이다:
 * - MASTER는 EDITOR, READER 권한을 포함
 * - EDITOR는 READER 권한을 포함
 *
 * 사용 예:
 * <pre>
 * // 편집자 이상만 접근 가능
 * {@code @RequireRole(Role.ROLE_EDITOR)}
 * {@code @PostMapping("/posts")}
 * public void createPost(...) { ... }
 *
 * // 마스터만 접근 가능 (시스템 설정 등)
 * {@code @RequireRole(Role.ROLE_MASTER)}
 * {@code @DeleteMapping("/users/{id}")}
 * public void deleteUser(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 필요한 최소 역할 */
    Role value();
}
