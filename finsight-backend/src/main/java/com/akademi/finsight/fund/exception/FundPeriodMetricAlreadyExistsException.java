package com.akademi.finsight.fund.exception;

import com.akademi.finsight.common.exception.BaseException;

public class FundPeriodMetricAlreadyExistsException extends BaseException {

    public FundPeriodMetricAlreadyExistsException() {
        super(FundErrorType.FUND_PERIOD_METRIC_ALREADY_EXISTS);
    }
}
