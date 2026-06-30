package com.deoksan.share.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Jackson ObjectMapper 전역 래퍼.
 *
 * <p>앱 전체에서 공유되는 설정된 ObjectMapper를 주입받아 사용한다.
 * {@code new ObjectMapper()} 직접 생성을 금지한다 —
 * Spring Boot 자동설정(날짜 포맷, null 처리, 모듈 등록 등)이 무시된다.
 *
 * <h3>사용 예</h3>
 * <pre>
 * // 객체 → JSON 문자열
 * String json = jsonUtils.toJson(post);
 *
 * // JSON 문자열 → 객체
 * Post post = jsonUtils.fromJson(json, Post.class);
 *
 * // 제네릭 컬렉션 역직렬화
 * List&lt;Post&gt; posts = jsonUtils.fromJson(json, new TypeReference&lt;List&lt;Post&gt;&gt;() {});
 *
 * // Map → DTO 변환 (OAuth2 속성 매핑 등)
 * UserDto dto = jsonUtils.convert(attributeMap, UserDto.class);
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class JsonUtils {

    private final ObjectMapper objectMapper;

    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization failed: " + e.getOriginalMessage(), e);
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON deserialization failed: " + e.getOriginalMessage(), e);
        }
    }

    public <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON deserialization failed: " + e.getOriginalMessage(), e);
        }
    }

    /** Map ↔ POJO 변환. JSON 문자열을 거치지 않고 직접 변환한다. */
    public <T> T convert(Object source, Class<T> targetClass) {
        return objectMapper.convertValue(source, targetClass);
    }
}
