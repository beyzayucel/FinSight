package com.akademi.finsight.auth.ratelimiter.service;

import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;

public interface LoginRateLimitService {
    void incrementFailedAttempts(String hashedIdentifier);
    void checkAttemptsOrThrow(String hashedIdentifier) throws RateLimitException;
    void resetAttempts(String hashedIdentifier);
}
