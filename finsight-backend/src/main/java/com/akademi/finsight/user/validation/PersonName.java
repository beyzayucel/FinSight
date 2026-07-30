package com.akademi.finsight.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PersonNameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface PersonName {

    String message() default "{validation.name.format}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
