package com.akademi.finsight.auth.verificationtoken.service;

import com.akademi.finsight.user.entity.User;

public interface VerificationTokenService {
    void createAndSendVerificationToken(User user, String temporaryPassword);
    int deleteExpiredTokens();
}
