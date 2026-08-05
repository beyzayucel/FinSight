package com.akademi.finsight.user.exception;

import com.akademi.finsight.common.exception.BaseException;

public class AdminProtectedException extends BaseException {

    public AdminProtectedException() {
        super(UserErrorType.ADMIN_PROTECTED);
    }
}
