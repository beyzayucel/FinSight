package com.akademi.finsight.security.jwt.service;

import java.time.Instant;

/**
 * Sifre degistiginde o ana kadar uretilmis access token'lari gecersiz kilar.
 * Refresh token'lar veritabaninda iptal edilebiliyor ama access token stateless
 * oldugu icin, kontrol edilmezse suresi dolana kadar (varsayilan 15 dk) gecerli kalirdi.
 */
public interface TokenInvalidationService {

    /** Bu andan onceki tum access token'lari gecersiz sayar. */
    void invalidateTokensIssuedBefore(String username);

    boolean isInvalidated(String username, Instant issuedAt);
}
