package com.akademi.finsight.otp.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import com.akademi.finsight.common.exception.BaseException;

public class NotificationSendException extends BaseException {

    public NotificationSendException(BaseErrorType errorType) {
        super(errorType);
    }

    public NotificationSendException(BaseErrorType errorType, Throwable cause) {
        super(errorType, cause);
    }

    public NotificationSendException(Throwable cause) {
        super(OtpErrorType.NOTIFICATION_SEND_FAILED, cause);
    }
}
