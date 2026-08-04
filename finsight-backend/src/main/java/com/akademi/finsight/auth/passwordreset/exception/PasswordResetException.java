package com.akademi.finsight.auth.passwordreset.exception;

import com.akademi.finsight.common.exception.BaseException;

public class PasswordResetException extends BaseException {

    public PasswordResetException(PasswordResetErrorType errorType) {
        super(errorType);
    }

    public PasswordResetException(PasswordResetErrorType errorType, Throwable cause) {
        super(errorType, cause);
    }
}
