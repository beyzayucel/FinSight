package com.akademi.finsight.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = InternationalPhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface InternationalPhone {

    String message() default "{validation.phone.pattern}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
