package com.akademi.finsight.security.jwt.exception;


import com.akademi.finsight.common.exception.BaseException;

public class JwtTokenException extends BaseException {

    public JwtTokenException(JwtErrorType errorType) {
        super(errorType);
    }

    public JwtTokenException(JwtErrorType errorType, Throwable cause) {
        super(errorType, cause);
    }
}
