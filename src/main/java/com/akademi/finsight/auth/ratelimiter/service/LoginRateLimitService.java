package com.akademi.finsight.auth.ratelimiter.service;

import com.akademi.finsight.auth.ratelimiter.exception.RateLimitExceededException;

public interface LoginRateLimitService {
    void incrementFailedAttempts(String hashedIdentifier);
    void checkAttemptsOrThrow(String hashedIdentifier) throws RateLimitExceededException;
    void resetAttempts(String hashedIdentifier);
}
