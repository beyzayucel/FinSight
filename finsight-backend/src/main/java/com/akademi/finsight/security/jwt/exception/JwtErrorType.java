package com.akademi.finsight.security.jwt.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JwtErrorType implements BaseErrorType {

    JWT_EXPIRED("error.jwt.expired", HttpStatus.UNAUTHORIZED),
    JWT_INVALID_SIGNATURE("error.jwt.invalid.signature", HttpStatus.UNAUTHORIZED),
    JWT_MALFORMED("error.jwt.malformed", HttpStatus.BAD_REQUEST),
    JWT_UNSUPPORTED("error.jwt.unsupported", HttpStatus.BAD_REQUEST),
    JWT_GENERAL("error.jwt.general", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}
