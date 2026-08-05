package com.akademi.finsight.fund.exception;

import com.akademi.finsight.common.exception.BaseException;

public class FundNotFoundException extends BaseException {

    public FundNotFoundException() {
        super(FundErrorType.FUND_NOT_FOUND);
    }
}
