package com.akademi.finsight.user.exception;

import com.akademi.finsight.common.exception.BaseException;

public class EmailAlreadyVerifiedException extends BaseException {

    public EmailAlreadyVerifiedException() {
        super(UserErrorType.EMAIL_ALREADY_VERIFIED);
    }
}
