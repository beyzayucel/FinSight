package com.akademi.finsight.auth;

import com.akademi.finsight.audit.entity.AuditActionType;
import com.akademi.finsight.audit.service.AuditLogService;
import com.akademi.finsight.auth.dto.login.LoginRequest;
import com.akademi.finsight.auth.dto.login.LoginResult;
import com.akademi.finsight.auth.dto.login.OtpLoginRequest;
import com.akademi.finsight.auth.dto.login.ResendOtpRequest;
import com.akademi.finsight.auth.dto.password.ChangePasswordRequest;
import com.akademi.finsight.auth.dto.password.ForgotPasswordRequest;
import com.akademi.finsight.auth.dto.password.ResetPasswordRequest;
import com.akademi.finsight.auth.exception.AuthErrorType;
import com.akademi.finsight.auth.exception.AuthException;
import com.akademi.finsight.auth.otp.service.OtpService;
import com.akademi.finsight.auth.passwordhistory.service.PasswordHistoryService;
import com.akademi.finsight.auth.passwordreset.service.PasswordResetTokenService;
import com.akademi.finsight.auth.ratelimiter.config.LoginRateLimitProperties;
import com.akademi.finsight.auth.ratelimiter.service.LoginRateLimitService;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResponse;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResult;
import com.akademi.finsight.auth.refreshtoken.entity.RefreshToken;
import com.akademi.finsight.auth.refreshtoken.service.RefreshTokenService;
import com.akademi.finsight.auth.service.impl.AuthServiceImpl;
import com.akademi.finsight.monitoring.AppMetrics;
import com.akademi.finsight.security.jwt.service.TokenInvalidationService;
import com.akademi.finsight.auth.verificationtoken.service.VerificationTokenService;
import com.akademi.finsight.notification.service.NotificationService;
import com.akademi.finsight.security.jwt.service.JwtService;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.exception.UserErrorType;
import com.akademi.finsight.user.exception.UserException;
import com.akademi.finsight.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private LoginRateLimitService loginRateLimitService;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private OtpService otpService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private LoginRateLimitProperties loginRateLimitProperties;

    @Mock
    private PasswordResetTokenService passwordResetTokenService;

    @Mock
    private PasswordHistoryService passwordHistoryService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AppMetrics appMetrics;

    @Mock
    private TokenInvalidationService tokenInvalidationService;

    @InjectMocks
    private AuthServiceImpl authService;




    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should return Authenticated when first login (OTP bypassed)")
        void shouldReturnAuthenticatedWhenFirstLogin() {
            LoginRequest request = new LoginRequest("ali@test.com", "Pass123!");
            User user = createVerifiedUser("ali@test.com", true);

            Authentication auth = authenticatedPrincipal(user.getEmail());
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(userService.findByEmail(user.getEmail())).thenReturn(user);
            stubTokenGeneration(user);

            LoginResult result = authService.login(request);

            assertInstanceOf(LoginResult.Authenticated.class, result);
            LoginResult.Authenticated authenticated = (LoginResult.Authenticated) result;
            assertEquals("access-token", authenticated.accessToken());
            assertEquals("raw-refresh", authenticated.refreshToken());
            assertTrue(authenticated.firstLogin());

            verify(loginRateLimitService).resetAttempts(request.identifier());
            verify(userService).updateLastLogin(user);
            verify(auditLogService).createAuditLogForSelf(AuditActionType.LOGIN_SUCCESS, user);
            verifyNoInteractions(otpService);
        }

        @Test
        @DisplayName("should return OtpRequired when not first login")
        void shouldReturnOtpRequiredWhenNotFirstLogin() {
            LoginRequest request = new LoginRequest("ali@test.com", "Pass123!");
            User user = createVerifiedUser("ali@test.com", false);

            Authentication auth = authenticatedPrincipal(user.getEmail());
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(userService.findByEmail(user.getEmail())).thenReturn(user);

            LoginResult result = authService.login(request);

            assertInstanceOf(LoginResult.OtpRequired.class, result);

            verify(otpService).generateOtp(eq(user.getEmail()), eq(user.getFirstName()), any());
            verify(loginRateLimitService).resetAttempts(request.identifier());
            verifyNoInteractions(jwtService, refreshTokenService);
        }

        @Test
        @DisplayName("should throw EMAIL_NOT_VERIFIED when email not verified")
        void shouldThrowWhenEmailNotVerified() {
            LoginRequest request = new LoginRequest("ali@test.com", "Pass123!");
            User user = createUser("ali@test.com", false, false);

            Authentication auth = authenticatedPrincipal(user.getEmail());
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(userService.findByEmail(user.getEmail())).thenReturn(user);

            AuthException exception = assertThrows(AuthException.class, () -> authService.login(request));
            assertEquals(AuthErrorType.EMAIL_NOT_VERIFIED, exception.getErrorType());

            verifyNoInteractions(jwtService, refreshTokenService, otpService);
        }

        @Test
        @DisplayName("should increment failed attempts when authentication fails")
        void shouldIncrementFailedAttemptsWhenAuthFails() {
            LoginRequest request = new LoginRequest("ali@test.com", "wrongPass");

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            when(loginRateLimitService.incrementFailedAttempts(request.identifier())).thenReturn(false);

            assertThrows(BadCredentialsException.class, () -> authService.login(request));

            verify(loginRateLimitService).incrementFailedAttempts(request.identifier());
            verifyNoInteractions(jwtService, refreshTokenService, otpService, notificationService);
        }

        @Test
        @DisplayName("should send account locked notification when rate limit exceeded")
        void shouldSendLockedNotificationWhenBlocked() {
            LoginRequest request = new LoginRequest("ali@test.com", "wrongPass");
            User user = createVerifiedUser("ali@test.com", false);

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            when(loginRateLimitService.incrementFailedAttempts(request.identifier())).thenReturn(true);
            when(userService.findByIdentifier(request.identifier())).thenReturn(user);
            when(loginRateLimitProperties.getMaxAttempts()).thenReturn(5);
            when(loginRateLimitProperties.getBlockDuration()).thenReturn(Duration.ofMinutes(15));

            assertThrows(BadCredentialsException.class, () -> authService.login(request));

            verify(notificationService).notify(any());
            verifyNoInteractions(jwtService, refreshTokenService, otpService);
        }

        @Test
        @DisplayName("should throw INVALID_CREDENTIALS when principal is not UserDetails")
        void shouldThrowWhenPrincipalNotUserDetails() {
            LoginRequest request = new LoginRequest("ali@test.com", "Pass123!");

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn("not-a-userdetails");
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            AuthException exception = assertThrows(AuthException.class, () -> authService.login(request));
            assertEquals(AuthErrorType.INVALID_CREDENTIALS, exception.getErrorType());

            verify(loginRateLimitService).incrementFailedAttempts(request.identifier());
            verifyNoInteractions(jwtService, refreshTokenService, otpService);
        }
    }




    @Nested
    @DisplayName("otpLogin")
    class OtpLogin {

        @Test
        @DisplayName("should return Authenticated when OTP is valid")
        void shouldReturnAuthenticatedWhenOtpValid() {
            OtpLoginRequest request = new OtpLoginRequest("ali@test.com", "123456");
            User user = createVerifiedUser("ali@test.com", false);

            when(userService.findByIdentifier(request.identifier())).thenReturn(user);
            when(otpService.validateActiveOtp(user.getEmail())).thenReturn(true);
            stubTokenGeneration(user);

            LoginResult.Authenticated result = authService.otpLogin(request);

            assertEquals("access-token", result.accessToken());

            verify(otpService).validateOtp(user.getEmail(), request.code());
            verify(auditLogService).createAuditLogForSelf(AuditActionType.LOGIN_SUCCESS, user);
        }

        @Test
        @DisplayName("should throw OTP_NOT_ELIGIBLE when no active OTP")
        void shouldThrowWhenNoActiveOtp() {
            OtpLoginRequest request = new OtpLoginRequest("ali@test.com", "123456");
            User user = createVerifiedUser("ali@test.com", false);

            when(userService.findByIdentifier(request.identifier())).thenReturn(user);
            when(otpService.validateActiveOtp(user.getEmail())).thenReturn(false);

            AuthException exception = assertThrows(AuthException.class, () -> authService.otpLogin(request));
            assertEquals(AuthErrorType.OTP_NOT_ELIGIBLE, exception.getErrorType());

            verify(otpService, never()).validateOtp(any(), any());
            verifyNoInteractions(jwtService, refreshTokenService, auditLogService);
        }
    }




    @Nested
    @DisplayName("resendOtp")
    class ResendOtp {

        @Test
        @DisplayName("should resend OTP when active OTP exists")
        void shouldResendWhenActiveOtpExists() {
            ResendOtpRequest request = new ResendOtpRequest("ali@test.com");
            User user = createVerifiedUser("ali@test.com", false);

            when(userService.findByIdentifier(request.identifier())).thenReturn(user);
            when(otpService.validateActiveOtp(user.getEmail())).thenReturn(true);

            assertDoesNotThrow(() -> authService.resendOtp(request));

            verify(otpService).generateOtp(eq(user.getEmail()), eq(user.getFirstName()), any());
        }

        @Test
        @DisplayName("should throw OTP_NOT_ELIGIBLE when no active OTP")
        void shouldThrowWhenNoActiveOtp() {
            ResendOtpRequest request = new ResendOtpRequest("ali@test.com");
            User user = createVerifiedUser("ali@test.com", false);

            when(userService.findByIdentifier(request.identifier())).thenReturn(user);
            when(otpService.validateActiveOtp(user.getEmail())).thenReturn(false);

            AuthException exception = assertThrows(AuthException.class, () -> authService.resendOtp(request));
            assertEquals(AuthErrorType.OTP_NOT_ELIGIBLE, exception.getErrorType());

            verify(otpService, never()).generateOtp(any(), any(), any());
        }
    }




    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("should change password when current password matches")
        void shouldSucceedWhenCurrentPasswordMatches() {
            ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass123!");
            User user = createVerifiedUser("ali@test.com", false);

            when(userService.findByEmail("ali@test.com")).thenReturn(user);
            when(passwordEncoder.matches("oldPass", user.getPassword())).thenReturn(true);
            when(passwordEncoder.matches("newPass123!", user.getPassword())).thenReturn(false);
            when(passwordEncoder.encode("newPass123!")).thenReturn("encodedNewPass");

            assertDoesNotThrow(() -> authService.changePassword(request, "ali@test.com"));

            verify(userService).updatePassword(user, "encodedNewPass", false);
            verify(refreshTokenService).revokeAllByUser(user);
            verify(auditLogService).createAuditLogForSelf(AuditActionType.PASSWORD_CHANGED, user);
        }

        @Test
        @DisplayName("should throw WRONG_CURRENT_PASSWORD when current password is wrong")
        void shouldThrowWhenCurrentPasswordWrong() {
            ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "newPass123!");
            User user = createVerifiedUser("ali@test.com", false);

            when(userService.findByEmail("ali@test.com")).thenReturn(user);
            when(passwordEncoder.matches("wrongPass", user.getPassword())).thenReturn(false);

            AuthException exception = assertThrows(AuthException.class,
                    () -> authService.changePassword(request, "ali@test.com"));
            assertEquals(AuthErrorType.WRONG_CURRENT_PASSWORD, exception.getErrorType());

            verify(userService, never()).updatePassword(any(), any(), anyBoolean());
            verifyNoInteractions(refreshTokenService, auditLogService);
        }

        @Test
        @DisplayName("should throw SAME_PASSWORD when new password same as current")
        void shouldThrowWhenSamePassword() {
            ChangePasswordRequest request = new ChangePasswordRequest("samePass", "samePass");
            User user = createVerifiedUser("ali@test.com", false);

            when(userService.findByEmail("ali@test.com")).thenReturn(user);
            when(passwordEncoder.matches("samePass", user.getPassword())).thenReturn(true);

            AuthException exception = assertThrows(AuthException.class,
                    () -> authService.changePassword(request, "ali@test.com"));
            assertEquals(AuthErrorType.SAME_PASSWORD, exception.getErrorType());

            verify(userService, never()).updatePassword(any(), any(), anyBoolean());
            verifyNoInteractions(refreshTokenService, auditLogService);
        }
    }




    @Nested
    @DisplayName("refreshTokens")
    class RefreshTokens {

        @Test
        @DisplayName("should return new tokens when refresh token is valid")
        void shouldReturnNewTokensWhenValid() {
            RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");
            User user = createVerifiedUser("ali@test.com", false);
            RefreshToken refreshToken = RefreshToken.builder().user(user).build();

            when(refreshTokenService.rotateToken(request))
                    .thenReturn(new RefreshTokenResult("new-refresh", refreshToken));

            UserDetails userDetails = mock(UserDetails.class);
            when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);
            when(jwtService.generateAccessToken(userDetails, false)).thenReturn("new-access");
            when(jwtService.getAccessTokenExpiryMinutes()).thenReturn(15L);

            RefreshTokenResponse response = authService.refreshTokens(request);

            assertEquals("new-access", response.accessToken());
            assertEquals("new-refresh", response.refreshToken());
            assertEquals(15L, response.expiresIn());
        }
    }




    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("should revoke refresh token")
        void shouldRevokeRefreshToken() {
            RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

            assertDoesNotThrow(() -> authService.logout(request));

            verify(refreshTokenService).revokeToken(request);
            verifyNoMoreInteractions(refreshTokenService);
        }
    }





    @Nested
    @DisplayName("verifyEmail")
    class VerifyEmail {

        @Test
        @DisplayName("should delegate to verificationTokenService")
        void shouldDelegate() {
            assertDoesNotThrow(() -> authService.verifyEmail("token-123"));

            verify(verificationTokenService).verifyEmail("token-123");
            verifyNoMoreInteractions(verificationTokenService);
        }
    }




    @Nested
    @DisplayName("forgotPassword")
    class ForgotPassword {

        @Test
        @DisplayName("should send reset token when email is verified")
        void shouldSendResetTokenWhenEmailVerified() {
            ForgotPasswordRequest request = new ForgotPasswordRequest("ali@test.com");
            User user = createVerifiedUser("ali@test.com", false);

            when(userService.findByEmail("ali@test.com")).thenReturn(user);

            assertDoesNotThrow(() -> authService.forgotPassword(request));

            verify(passwordResetTokenService).createAndSendResetToken(user);
        }

        @Test
        @DisplayName("should not send reset token when email is not verified")
        void shouldNotSendWhenEmailNotVerified() {
            ForgotPasswordRequest request = new ForgotPasswordRequest("ali@test.com");
            User user = createUser("ali@test.com", false, false);

            when(userService.findByEmail("ali@test.com")).thenReturn(user);

            assertDoesNotThrow(() -> authService.forgotPassword(request));

            verifyNoInteractions(passwordResetTokenService);
        }

        @Test
        @DisplayName("should not throw when user not found (no email enumeration)")
        void shouldNotThrowWhenUserNotFound() {
            ForgotPasswordRequest request = new ForgotPasswordRequest("unknown@test.com");

            when(userService.findByEmail("unknown@test.com"))
                    .thenThrow(new UserException(UserErrorType.USER_NOT_FOUND));

            assertDoesNotThrow(() -> authService.forgotPassword(request));

            verifyNoInteractions(passwordResetTokenService);
        }
    }



    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("should reset password and revoke all tokens")
        void shouldResetAndRevokeTokens() {
            ResetPasswordRequest request = new ResetPasswordRequest("reset-token", "NewPass456!");
            User user = createVerifiedUser("ali@test.com", false);

            when(passwordResetTokenService.consumeToken("reset-token")).thenReturn(user);
            when(passwordEncoder.encode("NewPass456!")).thenReturn("encodedNewPass");

            assertDoesNotThrow(() -> authService.resetPassword(request));

            verify(userService).updatePassword(user, "encodedNewPass", true);
            verify(refreshTokenService).revokeAllByUser(user);
            verify(auditLogService).createAuditLogForSelf(AuditActionType.PASSWORD_RESET_COMPLETED, user);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // helpers
    // ══════════════════════════════════════════════════════════════

    private User createVerifiedUser(String email, boolean firstLogin) {
        return createUser(email, true, firstLogin);
    }

    private User createUser(String email, boolean emailVerified, boolean firstLogin) {
        return User.builder()
                .email(email)
                .username(email.split("@")[0])
                .password("encodedPassword")
                .firstName("Ali")
                .lastName("Test")
                .phoneNumber("5551234567")
                .emailVerified(emailVerified)
                .firstLogin(firstLogin)
                .build();
    }

    private Authentication authenticatedPrincipal(String email) {
        UserDetails userDetails = mockUserDetails(email);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        return auth;
    }

    private UserDetails mockUserDetails(String email) {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(email);
        return userDetails;
    }

    private void stubTokenGeneration(User user) {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails, user.isFirstLogin())).thenReturn("access-token");
        when(jwtService.getAccessTokenExpiryMinutes()).thenReturn(15L);
        when(refreshTokenService.createAndSave(user))
                .thenReturn(new RefreshTokenResult("raw-refresh", RefreshToken.builder().user(user).build()));
    }
}
