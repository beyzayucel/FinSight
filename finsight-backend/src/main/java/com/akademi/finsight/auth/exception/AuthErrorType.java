package com.akademi.finsight.auth.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorType implements BaseErrorType {

    INVALID_CREDENTIALS("error.invalid.credentials", HttpStatus.UNAUTHORIZED),
    WRONG_CURRENT_PASSWORD("error.wrong.current.password", HttpStatus.BAD_REQUEST),
    SAME_PASSWORD("error.same.password", HttpStatus.BAD_REQUEST),
    PASSWORD_RECENTLY_USED("error.password.recently.used", HttpStatus.BAD_REQUEST),
    OTP_NOT_ELIGIBLE("error.otp.not.eligible", HttpStatus.FORBIDDEN),
    EMAIL_NOT_VERIFIED("error.email.not.verified", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED("error.account.locked", HttpStatus.TOO_MANY_REQUESTS);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}
