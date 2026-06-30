package com.deoksan.share.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Common request object for offset-based pagination.
 *
 * Use as a {@code @Valid} parameter in controllers to enforce
 * page/size constraints consistently across all endpoints.
 *
 * Example:
 * <pre>
 * {@code @GetMapping}
 * public PageResponse<PostDto> list(@Valid PageRequest req) { ... }
 * </pre>
 */
@Getter
@Setter
public class ValidPageRequest {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE     = 100;
    public static final String DEFAULT_SIZE_STR = "20"; // for use in @RequestParam(defaultValue = ...)

    @Min(value = 0, message = "{validation.page.min}")
    private int page = 0;

    @Min(value = 1, message = "{validation.page.size.min}")
    @Max(value = MAX_SIZE, message = "{validation.page.size.max}")
    private int size = DEFAULT_SIZE;

    public ValidPageRequest() {
    }

    public ValidPageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }
}
