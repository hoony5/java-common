package com.deoksan.share.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Offset-based paginated response.
 *
 * Suitable for traditional page navigation (1, 2, 3...) and works universally
 * across web, mobile, and API consumers.
 *
 * @param <T> the type of content elements
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;
    private final boolean isFirst;
    private final boolean isLast;

    /**
     * Creates a PageResponse from Spring Data's Page abstraction.
     *
     * @param page Spring Data Page object
     * @param mapper function to convert entity to DTO
     * @param <T> DTO type
     * @param <E> Entity type
     * @return PageResponse containing mapped DTOs
     */
    public static <T, E> PageResponse<T> from(
            org.springframework.data.domain.Page<E> page,
            java.util.function.Function<E, T> mapper
    ) {
        List<T> content = page.getContent().stream()
                .map(mapper)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious(),
                page.isFirst(),
                page.isLast()
        );
    }

    /**
     * Factory for empty page.
     */
    public static <T> PageResponse<T> empty(int page, int size) {
        return new PageResponse<>(
                List.of(), page, size, 0, 0,
                false, false, true, true
        );
    }
}
