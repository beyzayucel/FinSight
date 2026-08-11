package com.akademi.finsight.fund.exception;

import com.akademi.finsight.common.exception.BaseException;

public class FundStockAllocationNotFoundException extends BaseException {

    public FundStockAllocationNotFoundException() {
        super(FundErrorType.FUND_STOCK_ALLOCATION_NOT_FOUND);
    }
}
