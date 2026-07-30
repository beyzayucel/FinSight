package com.akademi.finsight.user.exception;


import com.akademi.finsight.common.exception.BaseException;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException() {
        super(UserErrorType.USER_NOT_FOUND);
    }
}
