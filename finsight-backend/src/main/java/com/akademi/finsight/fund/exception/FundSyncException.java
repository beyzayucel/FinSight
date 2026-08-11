package com.akademi.finsight.fund.exception;

import com.akademi.finsight.common.exception.BaseException;

public class FundSyncException extends BaseException {

    public FundSyncException() {
        super(FundErrorType.FUND_SYNC_FAILED);
    }
}
