package com.akademi.finsight.stresstest.exception;

import com.akademi.finsight.common.exception.BaseException;

public class StressTestException extends BaseException {

    public StressTestException(StressTestErrorType errorType) {
        super(errorType);
    }

    public StressTestException(StressTestErrorType errorType, Throwable cause) {
        super(errorType, cause);
    }
}
