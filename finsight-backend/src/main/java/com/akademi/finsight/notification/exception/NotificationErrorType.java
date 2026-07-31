package com.akademi.finsight.notification.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorType implements BaseErrorType {

    EMAIL_SENDING_FAILED("error.notification.email.sending.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_NOTIFICATION("error.notification.invalid", HttpStatus.INTERNAL_SERVER_ERROR),
    PUBLISH_FAILED("error.notification.publish.failed", HttpStatus.SERVICE_UNAVAILABLE);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}
