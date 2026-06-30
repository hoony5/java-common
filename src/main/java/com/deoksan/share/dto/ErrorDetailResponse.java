package com.deoksan.share.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 상세 에러 응답 DTO.
 *
 * <p>벨리데이션 실패나 비즈니스 규칙 위반 시
 * 어떤 필드가 왜 실패했는지 클라이언트에 알려준다.
 *
 * <h3>응답 예시</h3>
 * <pre>{@code
 * {
 *   "success": false,
 *   "message": "입력값이 유효하지 않습니다",
 *   "code": "VL-FD-001",
 *   "data": {
 *     "errors": [
 *       { "field": "title", "message": "제목은 필수입니다" },
 *       { "field": "content", "message": "내용은 최소 10자 이상이어야 합니다" }
 *     ]
 *   }
 * }
 * }</pre>
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorDetailResponse {

    private final List<FieldError> errors;

    public static ErrorDetailResponse of(List<FieldError> errors) {
        return new ErrorDetailResponse(errors);
    }

    public static ErrorDetailResponse of(String field, String message) {
        return new ErrorDetailResponse(List.of(new FieldError(field, message)));
    }

    /**
     * 필드 레벨 에러 정보.
     */
    @Getter
    @AllArgsConstructor
    public static class FieldError {

        /** 실패한 필드명 */
        private final String field;

        /** 에러 메시지 */
        private final String message;

        public static FieldError of(String field, String message) {
            return new FieldError(field, message);
        }
    }
}
