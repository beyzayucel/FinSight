package com.akademi.finsight.auth.controller.api;


import com.akademi.finsight.auth.dto.login.LoginRequest;
import com.akademi.finsight.auth.dto.login.LoginResponse;
import com.akademi.finsight.auth.dto.password.ChangePasswordRequest;
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
            description = "Authenticates a user with email or username and returns access and refresh tokens."
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping(ApiEndpoints.Auth.LOGIN)
    ResponseEntity<ApiStandardResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request);

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
}
