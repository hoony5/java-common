package com.deoksan.share.security;

import org.springframework.security.core.GrantedAuthority;

/**
 * 애플리케이션 역할(Role) 정의.
 *
 * {@link GrantedAuthority}를 직접 구현하여 Spring Security와 타입 안전하게 통합된다.
 * 필터에서 {@code Role.fromString(claim)} 으로 JWT role claim을 변환해 사용한다.
 *
 * <pre>
 * ROLE_MASTER:
 *   - 모든 CRUD 작업
 *   - 사용자 관리
 *   - 시스템 설정
 *
 * ROLE_EDITOR:
 *   - 포스트 작성/수정/삭제
 *   - 댓글 관리
 *   - (읽기 권한 포함)
 *
 * ROLE_READER:
 *   - 포스트/댓글 읽기
 *   - 프로필 조회
 * </pre>
 */
public enum Role implements GrantedAuthority {

    /**
     * 마스터 — 전체 시스템 관리 권한.
     * 사용자 관리, 설정 변경, 모든 CRUD 가능.
     */
    ROLE_MASTER("master"),

    /**
     * 편집자 — 콘텐츠 작성/수정/삭제 권한.
     * 포스트, 댓글 등 콘텐츠 관리 가능.
     */
    ROLE_EDITOR("editor"),

    /**
     * 읽기전용 — 콘텐츠 조회 권한.
     * 포스트 읽기, 프로필 조회만 가능.
     */
    ROLE_READER("reader");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** Spring Security GrantedAuthority — "ROLE_MASTER" 형태의 이름 반환. */
    @Override
    public String getAuthority() {
        return name();
    }

    /**
     * JWT role claim 문자열을 Role 열거형으로 안전하게 변환한다.
     *
     * @return 매핑된 Role, 알 수 없는 값이면 null
     */
    public static Role fromString(String authority) {
        if (authority == null) return null;
        try {
            return Role.valueOf(authority);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
