package com.akademi.finsight.fund.exception;

import com.akademi.finsight.common.exception.BaseException;

public class FundStockAllocationAlreadyExistsException extends BaseException {

    public FundStockAllocationAlreadyExistsException() {
        super(FundErrorType.FUND_STOCK_ALLOCATION_ALREADY_EXISTS);
    }
}
