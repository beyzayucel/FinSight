package com.akademi.finsight.notification.exception;

import com.akademi.finsight.common.exception.BaseException;

public class NotificationPublishException extends BaseException {
    public NotificationPublishException(Throwable cause) {
        super(NotificationErrorType.PUBLISH_FAILED, cause);
    }
}
