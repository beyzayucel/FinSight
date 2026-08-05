package com.akademi.finsight.user.exception;

import com.akademi.finsight.common.exception.BaseException;

public class UserException extends BaseException {

    public UserException(UserErrorType errorType) {
        super(errorType);
    }

}
