package com.akademi.finsight.news.dto.response;

public record NewsResponse(
        String title,
        String url,
        Long hoursAgo
) {
}
