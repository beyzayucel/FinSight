package com.akademi.finsight.auth.service;

import com.akademi.finsight.auth.dto.login.LoginRequest;
import com.akademi.finsight.auth.dto.login.LoginResponse;
import com.akademi.finsight.auth.dto.password.ChangePasswordRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refreshTokens(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void changePassword(ChangePasswordRequest request, String email);
}
