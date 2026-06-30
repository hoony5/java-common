package com.deoksan.share.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSON 응답에서 해당 필드를 마스킹 처리한다.
 *
 * 개인정보(이메일, 전화번호, 비밀번호 등)를 API 응답에서
 * 일부만 노출할 때 사용한다.
 *
 * 사용 예:
 * <pre>
 * {@code @Masked}
 * private String email;  // → "h***@gmail.com"
 *
 * {@code @Masked(maskChar = '#')}
 * private String phone;  // → "010####1234"
 * </pre>
 */
// FIELD: 일반 클래스 필드
// METHOD: record 컴포넌트의 accessor 메서드에 어노테이션 전파 허용
// RECORD_COMPONENT: Java 16+ record 컴포넌트에 직접 선언 허용
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Masked {

    /** 마스킹에 사용할 문자 */
    char maskChar() default '*';

    /** 노출할 앞자리 글자 수 */
    int prefixLength() default 1;

    /** 노출할 뒷자리 글자 수 */
    int suffixLength() default 4;
}
