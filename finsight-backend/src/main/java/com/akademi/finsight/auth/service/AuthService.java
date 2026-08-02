package com.akademi.finsight.auth.service;

import com.akademi.finsight.auth.dto.login.LoginRequest;
import com.akademi.finsight.auth.dto.login.LoginResult;
import com.akademi.finsight.auth.dto.login.OtpLoginRequest;
import com.akademi.finsight.auth.dto.login.ResendOtpRequest;
import com.akademi.finsight.auth.dto.password.ChangePasswordRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResponse;

public interface AuthService {

    LoginResult login(LoginRequest request);

    RefreshTokenResponse refreshTokens(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void changePassword(ChangePasswordRequest request, String email);

    void verifyEmail(String token);

    LoginResult.Authenticated otpLogin(OtpLoginRequest request);

    void resendOtp(ResendOtpRequest request);
}
