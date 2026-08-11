package com.akademi.finsight.ai.dto;

import java.util.List;

/** application/problem+json gövdesi. request_id, X-Request-ID header'ıyla aynıdır. */
public record ProblemDetails(
        String type,
        String title,
        Integer status,
        String detail,
        String instance,
        String code,
        String requestId,
        List<ProblemItem> errors
) {
    public record ProblemItem(String pointer, String code, String detail) {
    }
}
