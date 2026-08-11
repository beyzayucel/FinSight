package com.akademi.finsight.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType implements BaseErrorType {

    // Security (cross-cutting, used by filters)
    UNAUTHORIZED("error.unauthorized", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("error.access.denied", HttpStatus.FORBIDDEN),

    // Validation / Framework
    VALIDATION_ERROR("error.validation", HttpStatus.BAD_REQUEST),
    MALFORMED_JSON("error.malformed.json", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("error.method.not.allowed", HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE("error.unsupported.media.type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    MISSING_PARAMETER("error.missing.parameter", HttpStatus.BAD_REQUEST),
    PARAMETER_TYPE_MISMATCH("error.parameter.type.mismatch", HttpStatus.BAD_REQUEST),
    INVALID_INPUT("error.invalid.input", HttpStatus.BAD_REQUEST),
    ENDPOINT_NOT_FOUND("error.endpoint.not.found", HttpStatus.NOT_FOUND),

    // Data integrity
    DATA_CONFLICT("error.data.conflict", HttpStatus.CONFLICT),

    // General
    INTERNAL_ERROR("error.internal", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String messageKey;
    private final HttpStatus httpStatus;

    public String getCode() {
        return name();
    }
}
