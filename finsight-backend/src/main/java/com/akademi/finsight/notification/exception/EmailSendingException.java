package com.akademi.finsight.notification.exception;

import com.akademi.finsight.common.exception.BaseException;

public class EmailSendingException extends BaseException {
    public EmailSendingException(Throwable cause) {
        super(NotificationErrorType.EMAIL_SENDING_FAILED, cause);
    }
}
