package com.akademi.finsight.otp.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorType implements BaseErrorType {

    NOTIFICATION_SEND_FAILED("error.notification.email.sending.failed", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}