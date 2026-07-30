package com.akademi.finsight.auth.refreshtoken.exception;


import com.akademi.finsight.common.exception.BaseException;

public class RefreshTokenException extends BaseException {

    public RefreshTokenException(RefreshTokenErrorType errorType) {
        super(errorType);
    }

    public RefreshTokenException(RefreshTokenErrorType errorType, Throwable cause) {
        super(errorType, cause);
    }
}
