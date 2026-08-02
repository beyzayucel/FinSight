package com.akademi.finsight.otp.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OtpErrorType implements BaseErrorType {

    OTP_SEND_FAILED("error.otp.send.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    OTP_EXPIRED_OR_INVALID("error.otp.expired.or.invalid", HttpStatus.UNAUTHORIZED),
    OTP_INCORRECT("error.otp.incorrect", HttpStatus.UNAUTHORIZED),
    OTP_COOLDOWN_ACTIVE("error.otp.cooldown.active", HttpStatus.TOO_MANY_REQUESTS),
    OTP_MAX_ATTEMPTS_EXCEEDED("error.otp.max.attempts.exceeded", HttpStatus.TOO_MANY_REQUESTS);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}