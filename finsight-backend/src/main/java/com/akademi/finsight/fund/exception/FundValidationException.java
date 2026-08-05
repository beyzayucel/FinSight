package com.akademi.finsight.fund.exception;


import com.akademi.finsight.common.exception.BaseException;

public class FundValidationException extends BaseException {

    private final transient Object[] customArgs;

    public FundValidationException(FundErrorType errorType) {
        super(errorType);
        this.customArgs = new Object[0];
    }

    public FundValidationException(FundErrorType errorType, Object... args) {
        super(errorType);
        this.customArgs = args;
    }

    public Object[] getArgs() {
        return this.customArgs;
    }
}