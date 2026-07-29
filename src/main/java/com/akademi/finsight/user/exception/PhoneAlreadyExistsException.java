package com.akademi.finsight.user.exception;


import com.akademi.finsight.common.exception.BaseException;

public class PhoneAlreadyExistsException extends BaseException {

    public PhoneAlreadyExistsException() {
        super(UserErrorType.PHONE_ALREADY_EXISTS);
    }
}
