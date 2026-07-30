package com.akademi.finsight.user.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailNormalizer {

    public static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
