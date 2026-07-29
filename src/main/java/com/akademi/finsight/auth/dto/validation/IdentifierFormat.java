package com.akademi.finsight.auth.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IdentifierFormatValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IdentifierFormat {

    String message() default "{validation.identifier.format}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
