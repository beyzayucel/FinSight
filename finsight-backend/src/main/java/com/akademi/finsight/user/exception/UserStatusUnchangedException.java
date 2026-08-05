package com.akademi.finsight.user.exception;

import com.akademi.finsight.common.exception.BaseException;

public class UserStatusUnchangedException extends BaseException {

    public UserStatusUnchangedException() {
        super(UserErrorType.USER_STATUS_UNCHANGED);
    }
}
