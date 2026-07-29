package com.akademi.finsight.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordStrengthValidator implements ConstraintValidator<PasswordStrength, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 32;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            return false;
        }

        return containsLowercase(value)
                && containsUppercase(value)
                && containsDigit(value)
                && containsSpecialChar(value);
    }

    private boolean containsLowercase(String value) {
        return value.chars().anyMatch(Character::isLowerCase);
    }

    private boolean containsUppercase(String value) {
        return value.chars().anyMatch(Character::isUpperCase);
    }

    private boolean containsDigit(String value) {
        return value.chars().anyMatch(Character::isDigit);
    }

    private boolean containsSpecialChar(String value) {
        return value.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
    }
}
