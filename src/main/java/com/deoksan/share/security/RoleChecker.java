package com.deoksan.share.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 역할(Role) 체크 컴포넌트.
 *
 * <p>{@code @PreAuthorize("@roleChecker.hasAnyRole('ROLE_EDITOR','ROLE_MASTER')")}
 * 형태로 사용하며, {@code @RequireRole} 어노테이션과 함께
 * 메서드 레벨 보안을 적용한다.
 */
@Component
public class RoleChecker {

    /**
     * 현재 사용자가 지정된 역할 중 하나라도 가지고 있는지 확인.
     */
    public boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        for (GrantedAuthority ga : auth.getAuthorities()) {
            for (String role : roles) {
                if (ga.getAuthority().equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 현재 사용자가 지정된 역할을 가지고 있는지 확인.
     * SpEL: @roleChecker.hasRole('ROLE_MASTER')
     */
    public boolean hasRole(String role) {
        return hasAnyRole(role);
    }

    /**
     * Role enum을 직접 받는 오버로드 — SpEL에서 T() 문법으로 사용.
     * SpEL: @roleChecker.hasRole(T(com.deoksan.share.security.Role).ROLE_MASTER)
     */
    public boolean hasRole(Role role) {
        return hasAnyRole(role.getAuthority());
    }
}
