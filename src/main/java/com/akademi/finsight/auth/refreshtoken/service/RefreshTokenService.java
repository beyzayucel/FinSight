package com.akademi.finsight.auth.refreshtoken.service;


import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResult;
import com.akademi.finsight.user.entity.User;

public interface RefreshTokenService {

    RefreshTokenResult createAndSave(User user);

    RefreshTokenResult rotateToken(RefreshTokenRequest request);

    void revokeToken(RefreshTokenRequest request);

    void revokeAllByUser(User user);
}
