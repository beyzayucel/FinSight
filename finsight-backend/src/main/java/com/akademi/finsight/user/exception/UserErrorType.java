package com.akademi.finsight.user.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorType implements BaseErrorType {

    USER_NOT_FOUND("error.user.not.found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("error.email.already.exists", HttpStatus.CONFLICT),
    PHONE_ALREADY_EXISTS("error.phone.already.exists", HttpStatus.CONFLICT),
    USER_STATUS_UNCHANGED("error.user.status.unchanged", HttpStatus.CONFLICT),
    EMAIL_ALREADY_VERIFIED("error.email.already.verified", HttpStatus.CONFLICT);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}
