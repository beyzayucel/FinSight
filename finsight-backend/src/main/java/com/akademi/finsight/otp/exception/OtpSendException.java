package com.akademi.finsight.otp.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import com.akademi.finsight.common.exception.BaseException;

public class OtpSendException extends BaseException {
    public OtpSendException(Throwable cause) {
        super(OtpErrorType.NOTIFICATION_SEND_FAILED, cause);
    }
}
