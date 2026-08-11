package com.akademi.finsight.auth.controller.api;


import com.akademi.finsight.auth.dto.login.LoginRequest;
import com.akademi.finsight.auth.dto.login.LoginResult;
import com.akademi.finsight.auth.dto.login.OtpLoginRequest;
import com.akademi.finsight.auth.dto.login.ResendOtpRequest;
import com.akademi.finsight.auth.dto.password.ChangePasswordRequest;
import com.akademi.finsight.auth.dto.password.ForgotPasswordRequest;
import com.akademi.finsight.auth.dto.password.ResetPasswordRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResponse;
import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RequestMapping(ApiEndpoints.Auth.BASE)
@Tag(
        name = "Authentication",
        description = "User login, token refresh, logout and password operations"
)
public interface AuthApi {

    @Operation(
            summary = "User login",
            description = "Authenticates a user. Returns JWT tokens on first login, or sends OTP for 2FA on subsequent logins."
    )
    @ApiResponse(responseCode = "200", description = "Login successful or OTP sent")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping(ApiEndpoints.Auth.LOGIN)
    ResponseEntity<ApiStandardResponse<LoginResult>> login(@Valid @RequestBody LoginRequest request);

    @Operation(
            summary = "Refresh access token",
            description = "Issues a new access token and rotates the refresh token. The old refresh token is revoked."
    )
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Refresh token expired or revoked")
    @ApiResponse(responseCode = "404", description = "Refresh token not found")
    @PostMapping(ApiEndpoints.Auth.REFRESH)
    ResponseEntity<ApiStandardResponse<RefreshTokenResponse>> refreshTokens(
            @Valid @RequestBody RefreshTokenRequest request);

    @Operation(
            summary = "Logout",
            description = "Revokes the refresh token, effectively logging the user out."
    )
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @ApiResponse(responseCode = "404", description = "Refresh token not found")
    @PostMapping(ApiEndpoints.Auth.LOGOUT)
    ResponseEntity<ApiStandardResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request);

    @Operation(
            summary = "Change password",
            description = "Changes the authenticated user's password. Revokes all existing refresh tokens after a successful change."
    )
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Wrong current password or new password same as current")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @PatchMapping(ApiEndpoints.Auth.CHANGE_PASSWORD)
    ResponseEntity<ApiStandardResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                              @AuthenticationPrincipal String email);

    @Operation(
            summary = "Verify email",
            description = "Verifies a user's email address using the token sent via verification email."
    )
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "400", description = "Token is invalid or expired")
    @GetMapping(ApiEndpoints.Auth.VERIFY)
    ResponseEntity<ApiStandardResponse<Void>> verifyEmail(@RequestParam String token);

    @Operation(
            summary = "OTP login",
            description = "Completes 2FA login by validating the OTP code sent to the user's email. Returns JWT tokens on success."
    )
    @ApiResponse(responseCode = "200", description = "OTP verified, login successful")
    @ApiResponse(responseCode = "401", description = "OTP code is invalid or expired")
    @ApiResponse(responseCode = "403", description = "No active OTP session")
    @ApiResponse(responseCode = "429", description = "Too many failed OTP attempts")
    @PostMapping(ApiEndpoints.Auth.OTP_VERIFY)
    ResponseEntity<ApiStandardResponse<LoginResult.Authenticated>> otpLogin(@Valid @RequestBody OtpLoginRequest request);

    @Operation(
            summary = "Resend OTP",
            description = "Resends the OTP code to the user's email. Requires an active OTP session from a prior login attempt."
    )
    @ApiResponse(responseCode = "200", description = "OTP resent successfully")
    @ApiResponse(responseCode = "403", description = "No active OTP session")
    @ApiResponse(responseCode = "429", description = "Too many failed OTP attempts")
    @PostMapping(ApiEndpoints.Auth.OTP_RESEND)
    ResponseEntity<ApiStandardResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request);

    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset link to the user's email address if an account with that email exists. Always returns success to avoid revealing account existence."
    )
    @ApiResponse(responseCode = "200", description = "Request accepted")
    @PostMapping(ApiEndpoints.Auth.FORGOT_PASSWORD)
    ResponseEntity<ApiStandardResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request);

    @Operation(
            summary = "Reset password",
            description = "Sets a new password using the token received via the password reset email. Revokes all existing refresh tokens after a successful reset."
    )
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @ApiResponse(responseCode = "400", description = "Token is invalid or expired")
    @PostMapping(ApiEndpoints.Auth.RESET_PASSWORD)
    ResponseEntity<ApiStandardResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request);
}
