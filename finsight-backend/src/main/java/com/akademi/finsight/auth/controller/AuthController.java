package com.akademi.finsight.auth.controller;


import com.akademi.finsight.auth.controller.api.AuthApi;
import com.akademi.finsight.auth.dto.login.LoginRequest;
import com.akademi.finsight.auth.dto.login.LoginResponse;
import com.akademi.finsight.auth.dto.password.ChangePasswordRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResponse;
import com.akademi.finsight.auth.service.AuthService;
import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController extends BaseController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<ApiStandardResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ok(authService.login(request));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<RefreshTokenResponse>> refreshTokens(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ok(authService.refreshTokens(request));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ok();
    }

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                                     @AuthenticationPrincipal String email) {
        authService.changePassword(request, email);
        return ok();
    }
}
