package com.deoksan.share.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 공통 날짜 범위 요청 DTO.
 *
 * 기간 기반 필터링이 필요한 모든 엔드포인트에서 재사용 가능.
 *
 * 사용 예:
 * <pre>
 * GET /api/posts?from=2025-01-01T00:00:00&to=2025-12-31T23:59:59
 *
 * {@code @GetMapping}
 * public PageResponse<PostDto> list(@Valid DateRangeRequest range) { ... }
 * </pre>
 */
@Getter
@Setter
public class DateRangeRequest {

    /** 시작 일시 (포함) */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime from;

    /** 종료 일시 (포함) */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime to;

    public boolean hasFrom() {
        return from != null;
    }

    public boolean hasTo() {
        return to != null;
    }

    public boolean hasRange() {
        return hasFrom() || hasTo();
    }
}
