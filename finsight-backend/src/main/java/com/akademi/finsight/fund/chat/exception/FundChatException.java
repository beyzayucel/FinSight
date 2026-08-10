package com.akademi.finsight.fund.chat.exception;

import com.akademi.finsight.common.exception.BaseException;

public class FundChatException extends BaseException {

    public FundChatException(FundChatErrorType errorType) {
        super(errorType);
    }

    public FundChatException(FundChatErrorType errorType, Throwable cause) {
        super(errorType, cause);
    }
}
