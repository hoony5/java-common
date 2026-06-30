package com.deoksan.share.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * 공통 정렬 요청 DTO.
 *
 * 컨트롤러에서 {@code @Valid} 파라미터로 받아 정렬 조건을 주입한다.
 *
 * 사용 예:
 * <pre>
 * GET /api/posts?sort=createdAt,desc&sort=title,asc
 *
 * {@code @GetMapping}
 * public PageResponse<PostDto> list(@Valid SortRequest sort) { ... }
 * </pre>
 */
@Getter
@Setter
public class SortRequest {

    /**
     * 정렬 조건 목록.
     * 형식: "property,direction" (방향은 선택, 기본값: asc)
     * 예: "createdAt,desc", "title"
     */
    private List<String> sort = new ArrayList<>();

    /**
     * Spring Data의 Sort 객체로 변환한다.
     */
    public Sort toSort() {
        if (sort.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String s : sort) {
            String[] parts = s.split(",");
            String property      = parts[0];
            String directionToken = parts.length > 1 ? parts[1] : null;
            Sort.Direction direction = directionToken != null
                    ? Sort.Direction.fromString(directionToken)
                    : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, property));
        }
        return Sort.by(orders);
    }
}
