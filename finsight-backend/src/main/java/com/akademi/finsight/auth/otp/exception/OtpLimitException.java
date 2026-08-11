package com.akademi.finsight.auth.otp.exception;

import com.akademi.finsight.common.exception.BaseException;

public class OtpLimitException extends BaseException {

    public OtpLimitException() {
        super(OtpErrorType.OTP_ABUSE_LOCKED);
    }
}
