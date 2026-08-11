package com.akademi.finsight.auth.passwordhistory.service;

import com.akademi.finsight.user.entity.User;

public interface PasswordHistoryService {

    /** Yeni sifre son N sifreden biriyse hata firlatir. */
    void assertNotRecentlyUsed(User user, String rawPassword);

    /** Yeni sifreyi gecmise yazar ve sinirin disinda kalan eski kayitlari siler. */
    void updatePasswordHistory(User user, String encodedPassword);
}
