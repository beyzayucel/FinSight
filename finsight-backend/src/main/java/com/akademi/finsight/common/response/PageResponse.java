package com.akademi.finsight.common.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<SortOrder> sort
) {

    public record SortOrder(String property, String direction) {}

    public static <T> PageResponse<T> of(Page<T> page) {
        List<SortOrder> sort = page.getSort().stream()
                .map(order -> new SortOrder(order.getProperty(), order.getDirection().name()))
                .toList();

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                sort
        );
    }
}
