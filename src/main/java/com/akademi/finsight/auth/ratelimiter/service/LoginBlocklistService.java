package com.akademi.finsight.auth.ratelimiter.service;

import com.akademi.finsight.auth.ratelimiter.exception.RateLimitExceededException;

public interface LoginBlocklistService {
    void blockUser(String hashedIdentifier);
    boolean isBlocked(String hashedIdentifier);
    Long getRemainingBlockTimeInSeconds(String hashedIdentifier);
    void checkBlockedOrThrow(String hashedIdentifier) throws RateLimitExceededException;
}
