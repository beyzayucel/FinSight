package com.akademi.finsight.user.exception;

import com.akademi.finsight.common.exception.BaseException;

public class SelfActionNotAllowedException extends BaseException {

    public SelfActionNotAllowedException() {
        super(UserErrorType.SELF_ACTION_NOT_ALLOWED);
    }
}
