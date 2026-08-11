package com.akademi.finsight.auth.ratelimiter.service;

import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;

/** Sifre sifirlama taleplerini hedef e-posta bazinda sinirlar. */
public interface PasswordResetRateLimitService {

    /**
     * Talebi sayar ve limit asildiysa hata firlatir.
     *
     * @param email talebin hedefi (mail bombardimanina karsi)
     */
    void checkAndCountOrThrow(String email) throws RateLimitException;
}
