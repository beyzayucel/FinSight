package com.akademi.finsight.user.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Pure utility for generating usernames and temporary passwords.
 * No Spring dependency — uniqueness checks belong in the service layer.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CredentialsGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIALS = "!@#$%&*";
    private static final String ALL_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS + SPECIALS;

    private static final int TEMPORARY_PASSWORD_LENGTH = 12;

    /**
     * Generates base username: {@code firstname.lastname}.
     * Caller is responsible for appending suffix if collision exists.
     */
    public static String generateBaseUsername(String firstName, String lastName) {
        return normalize(firstName) + "." + normalize(lastName);
    }

    /**
     * Generates a cryptographically secure temporary password.
     * Guarantees at least one character from each category
     * (uppercase, lowercase, digit, special).
     */
    public static String generateTemporaryPassword() {
        char[] password = new char[TEMPORARY_PASSWORD_LENGTH];

        password[0] = randomChar(UPPERCASE);
        password[1] = randomChar(LOWERCASE);
        password[2] = randomChar(DIGITS);
        password[3] = randomChar(SPECIALS);

        for (int i = 4; i < TEMPORARY_PASSWORD_LENGTH; i++) {
            password[i] = randomChar(ALL_CHARACTERS);
        }

        shuffle(password);
        return new String(password);
    }

    private static char randomChar(String pool) {
        return pool.charAt(SECURE_RANDOM.nextInt(pool.length()));
    }

    /**
     * Shuffles the characters using the Fisher-Yates algorithm.
     */
    private static void shuffle(char[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    private static String normalize(String input) {
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(Locale.ENGLISH);

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Name contains no valid characters");
        }

        return normalized;
    }
}
