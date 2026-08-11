package com.akademi.finsight.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class IdentifierFormatValidator implements ConstraintValidator<IdentifierFormat, String> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z][a-zA-Z0-9.]{2,99}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return EMAIL_PATTERN.matcher(value).matches() || USERNAME_PATTERN.matcher(value).matches();
    }
}
