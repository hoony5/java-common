package com.deoksan.share.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 읽기 전용 트랜잭션을 적용한다.
 *
 * 목록 조회, 상세 조회 등 데이터를 변경하지 않는 메서드에 사용한다.
 * Hibernate가 플러시를 건너뛰어 성능이 향상된다.
 *
 * 사용 예:
 * <pre>
 * {@code @ReadOnlyTransaction}
 * {@code public PageResponse<PostDto> list(PageRequest req) { ... }}
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnlyTransaction {
}
