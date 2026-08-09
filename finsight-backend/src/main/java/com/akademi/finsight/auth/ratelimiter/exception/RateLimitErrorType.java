package com.akademi.finsight.auth.ratelimiter.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RateLimitErrorType implements BaseErrorType {

    RATE_LIMIT_EXCEEDED(
            "error.rate.limit.exceeded",
            HttpStatus.TOO_MANY_REQUESTS
    ),

    PASSWORD_RESET_RATE_LIMIT_EXCEEDED(
            "error.rate.limit.password.reset.exceeded",
            HttpStatus.TOO_MANY_REQUESTS
    ),

    REQUEST_NOT_WRAPPED(
            "error.rate.limit.request.not.wrapped",
            HttpStatus.BAD_REQUEST
    ),

    IDENTIFIER_MISSING(
            "error.rate.limit.identifier.missing",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_REQUEST(
            "error.rate.limit.invalid.request",
            HttpStatus.BAD_REQUEST
    );

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}
