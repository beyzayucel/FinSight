package com.akademi.finsight.fund.exception;

import com.akademi.finsight.common.exception.BaseException;

public class FundPeriodMetricNotFoundException extends BaseException {

    public FundPeriodMetricNotFoundException() {
        super(FundErrorType.FUND_PERIOD_METRIC_NOT_FOUND);
    }
}
