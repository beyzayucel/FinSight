package com.akademi.finsight.auth.exception;

import com.akademi.finsight.common.exception.BaseException;

public class LoginLimitException extends BaseException {

    public LoginLimitException() {
        super(AuthErrorType.ACCOUNT_LOCKED);
    }
}
