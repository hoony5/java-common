package com.deoksan.share.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Cursor-based paginated response for infinite scroll / feed-style UIs.
 *
 * More efficient than offset pagination for large datasets and mobile apps,
 * as it avoids OFFSET-based deep page scans.
 *
 * @param <T>       the type of content elements
 * @param <C>       the type of cursor value (typically Long ID or Instant timestamp)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CursorPageResponse<T, C> {

    private final List<T> content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final C nextCursor;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final C previousCursor;

    private final int size;
    private final boolean hasNext;

    /**
     * Factory for a forward-only cursor page (most common case).
     *
     * @param content    current page items
     * @param nextCursor cursor to fetch the next page, or null if last page
     * @param size       requested page size
     * @param hasNext    whether more items exist after this page
     * @param <T>        content type
     * @param <C>        cursor type
     * @return cursor page response
     */
    public static <T, C> CursorPageResponse<T, C> next(
            List<T> content,
            C nextCursor,
            int size,
            boolean hasNext
    ) {
        return new CursorPageResponse<>(content, nextCursor, null, size, hasNext);
    }

    /**
     * Factory for empty cursor page.
     */
    public static <T, C> CursorPageResponse<T, C> empty(int size) {
        return new CursorPageResponse<>(List.of(), null, null, size, false);
    }
}
