package com.deoksan.share.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 생성된 리소스의 ID를 반환하는 응답.
 *
 * POST 요청으로 새 리소스를 생성한 후, 생성된 ID를 클라이언트에 알릴 때 사용.
 *
 * 사용 예:
 * <pre>
 * {@code @PostMapping}
 * public IdResponse createPost(@Valid CreatePostRequest req) {
 *     Long id = PostService.create(req);
 *     return new IdResponse(id);
 * }
 * </pre>
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdResponse {

    private final Long id;

    public static IdResponse of(Long id) {
        return new IdResponse(id);
    }
}
