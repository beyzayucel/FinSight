package com.akademi.finsight.auth.controller;


import com.akademi.finsight.auth.controller.api.AuthApi;
import com.akademi.finsight.auth.dto.login.LoginRequest;
import com.akademi.finsight.auth.dto.login.LoginResult;
import com.akademi.finsight.auth.dto.login.OtpLoginRequest;
import com.akademi.finsight.auth.dto.login.ResendOtpRequest;
import com.akademi.finsight.auth.dto.password.ChangePasswordRequest;
import com.akademi.finsight.auth.dto.password.ForgotPasswordRequest;
import com.akademi.finsight.auth.dto.password.ResetPasswordRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResponse;
import com.akademi.finsight.auth.service.AuthService;
import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class AuthController extends BaseController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<ApiStandardResponse<LoginResult>> login(LoginRequest request) {
        return ok(authService.login(request));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<RefreshTokenResponse>> refreshTokens(RefreshTokenRequest request) {
        return ok(authService.refreshTokens(request));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> logout(RefreshTokenRequest request) {
        authService.logout(request);
        return ok();
    }

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> changePassword(ChangePasswordRequest request, String email) {
        authService.changePassword(request, email);
        return ok();
    }

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> verifyEmail(String token) {
        authService.verifyEmail(token);
        return ok();
    }

    @Override
    public ResponseEntity<ApiStandardResponse<LoginResult.Authenticated>> otpLogin(OtpLoginRequest request) {
        return ok(authService.otpLogin(request));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> resendOtp(ResendOtpRequest request) {
        authService.resendOtp(request);
        return ok();
    }

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> forgotPassword(ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ok();
    }

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> resetPassword(ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ok();
    }
}
