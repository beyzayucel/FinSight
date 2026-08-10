package com.akademi.finsight.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PersonNameValidator implements ConstraintValidator<PersonName, String> {

    private static final int MAX_LENGTH = 50;
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^\\p{L}++(?:[ '\\-]\\p{L}++)*+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        return value.length() <= MAX_LENGTH
                && NAME_PATTERN.matcher(value).matches();
    }
}
