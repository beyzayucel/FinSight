package com.akademi.finsight.auth.otp.exception;

import com.akademi.finsight.common.exception.BaseException;
import lombok.Getter;

@Getter
public class OtpException extends BaseException {

    private final Long retryAfterSeconds;
    private final Integer remainingAttempts;

    public OtpException(OtpErrorType errorType) {
        super(errorType);
        this.retryAfterSeconds = null;
        this.remainingAttempts = null;
    }

    public OtpException(OtpErrorType errorType, Throwable cause) {
        super(errorType, cause);
        this.retryAfterSeconds = null;
        this.remainingAttempts = null;
    }

    public OtpException(OtpErrorType errorType, Long retryAfterSeconds) {
        super(errorType);
        this.retryAfterSeconds = retryAfterSeconds;
        this.remainingAttempts = null;
    }

    public OtpException(OtpErrorType errorType, int remainingAttempts) {
        super(errorType);
        this.retryAfterSeconds = null;
        this.remainingAttempts = remainingAttempts;
    }
}
