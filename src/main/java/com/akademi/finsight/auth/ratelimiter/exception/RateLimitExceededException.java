package com.akademi.finsight.auth.ratelimiter.exception;

public class RateLimitExceededException extends Exception{
    public RateLimitExceededException(String s, Long timeRemaining) {
    }
}
