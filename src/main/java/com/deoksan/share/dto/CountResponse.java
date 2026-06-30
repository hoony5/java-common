package com.deoksan.share.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 전체 개수만 반환하는 응답.
 *
 * 개수 확인 API나 메타데이터 응답에서 사용.
 *
 * 사용 예:
 * <pre>
 * {@code @GetMapping("/posts/count")}
 * public CountResponse countPosts() {
 *     return new CountResponse(PostService.count());
 * }
 * </pre>
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CountResponse {

    private final long count;

    public static CountResponse of(long count) {
        return new CountResponse(count);
    }
}
