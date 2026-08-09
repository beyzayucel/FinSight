package com.akademi.finsight.auth.ratelimiter.service;

import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;

import java.time.Duration;

public interface LoginBlocklistService {
    void blockUser(String hashedIdentifier);
    void blockUser(String hashedIdentifier, Duration blockDuration);
    boolean isBlocked(String hashedIdentifier);
    Long getRemainingBlockTimeInSeconds(String hashedIdentifier);
    void unblockUser(String hashedIdentifier);
    void checkBlockedOrThrow(String hashedIdentifier) throws RateLimitException;
}
