package com.akademi.finsight.auth.exception;

import com.akademi.finsight.common.exception.BaseException;

public class AuthException extends BaseException {

    public AuthException(AuthErrorType errorType) {
        super(errorType);
    }

    public AuthException(AuthErrorType errorType, Throwable cause) {
        super(errorType, cause);
    }
}
