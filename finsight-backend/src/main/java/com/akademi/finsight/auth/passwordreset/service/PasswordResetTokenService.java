package com.akademi.finsight.auth.passwordreset.service;

import com.akademi.finsight.user.entity.User;

public interface PasswordResetTokenService {
    void createAndSendResetToken(User user);
    String createResetUrl(User user);
    User consumeToken(String token);
    int deleteExpiredTokens();
}
