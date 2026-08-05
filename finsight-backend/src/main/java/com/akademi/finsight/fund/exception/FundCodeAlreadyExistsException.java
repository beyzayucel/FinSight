package com.akademi.finsight.fund.exception;

import com.akademi.finsight.common.exception.BaseException;

public class FundCodeAlreadyExistsException extends BaseException {

    public FundCodeAlreadyExistsException() {
        super(FundErrorType.FUND_CODE_ALREADY_EXISTS);
    }
}
