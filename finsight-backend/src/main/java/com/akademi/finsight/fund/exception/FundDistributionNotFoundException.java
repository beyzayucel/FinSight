package com.akademi.finsight.fund.exception;

import com.akademi.finsight.common.exception.BaseException;

public class FundDistributionNotFoundException extends BaseException {

    public FundDistributionNotFoundException() {
        super(FundErrorType.FUND_DISTRIBUTION_NOT_FOUND);
    }
}
