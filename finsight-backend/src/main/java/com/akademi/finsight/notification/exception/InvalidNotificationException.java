package com.akademi.finsight.notification.exception;

import com.akademi.finsight.common.exception.BaseException;

public class InvalidNotificationException extends BaseException {
    public InvalidNotificationException() {
        super(NotificationErrorType.INVALID_NOTIFICATION);
    }
}
