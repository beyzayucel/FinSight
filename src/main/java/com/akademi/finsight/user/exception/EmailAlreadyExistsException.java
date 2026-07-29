package com.akademi.finsight.user.exception;


import com.akademi.finsight.common.exception.BaseException;

public class EmailAlreadyExistsException extends BaseException {

    public EmailAlreadyExistsException() {
        super(UserErrorType.EMAIL_ALREADY_EXISTS);
    }
}
