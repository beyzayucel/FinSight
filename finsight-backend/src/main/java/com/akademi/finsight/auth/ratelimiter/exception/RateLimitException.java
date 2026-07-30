package com.akademi.finsight.auth.ratelimiter.exception;

import com.akademi.finsight.common.exception.BaseException;
import lombok.Getter;

@Getter
public class RateLimitException extends BaseException {
    private final Long remainingTime;


    public RateLimitException(RateLimitErrorType errorType) {
        super(errorType);
        this.remainingTime = null;
    }

    public RateLimitException(RateLimitErrorType errorType, Throwable cause) {
        super(errorType, cause);
        this.remainingTime = null;
    }

    public RateLimitException(RateLimitErrorType errorType, Long remainingTime) {
        super(errorType);
        this.remainingTime = remainingTime;
    }

    public RateLimitException(RateLimitErrorType errorType, Long remainingTime, Throwable cause) {
        super(errorType, cause);
        this.remainingTime = remainingTime;
    }
}
