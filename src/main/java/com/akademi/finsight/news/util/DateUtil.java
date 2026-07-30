package com.akademi.finsight.news.util;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtil {

    private static final DateTimeFormatter PUBLISH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Long getHoursAgo(String publishDateStr) {
        if (Objects.isNull(publishDateStr) || publishDateStr.isBlank()) {
            return null;
        }
        try {
            LocalDateTime publishDateTime = LocalDateTime.parse(publishDateStr, PUBLISH_DATE_FORMATTER);
            Instant publishInstant = publishDateTime.toInstant(ZoneOffset.UTC);
            return Duration.between(publishInstant, Instant.now()).toHours();
        } catch (Exception e) {
            return null;
        }
    }
}
