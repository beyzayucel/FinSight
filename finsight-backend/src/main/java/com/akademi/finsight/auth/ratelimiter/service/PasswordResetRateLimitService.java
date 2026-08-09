package com.akademi.finsight.auth.ratelimiter.service;

import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;

/** Sifre sifirlama taleplerini hem hedef e-posta hem cagiran IP bazinda sinirlar. */
public interface PasswordResetRateLimitService {

    /**
     * Talebi sayar ve limit asildiysa hata firlatir.
     *
     * @param email talebin hedefi (mail bombardimanina karsi)
     * @param clientIp cagiran adres (toplu talep uretimine karsi)
     */
    void checkAndCountOrThrow(String email, String clientIp) throws RateLimitException;

    /** Token gonderimini sayar; govdede kullaniciyi tanimlayan alan olmadigi icin yalnizca IP bazli. */
    void checkAndCountSubmitOrThrow(String clientIp) throws RateLimitException;
}
