package com.goldbyte.citieye_server.analytics.internal.dto;

import java.util.List;

/**
 * Generic paginated result wrapper for analytics list endpoints.
 */
public record PagedResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PagedResult<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PagedResult<>(content, page, size, totalElements, totalPages);
    }
}
