package com.akademi.finsight.auth.verificationtoken.exception;

import com.akademi.finsight.common.exception.BaseException;

public class VerificationTokenException extends BaseException {

    public VerificationTokenException(VerificationTokenErrorType errorType) {
        super(errorType);
    }

}
