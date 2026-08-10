package com.akademi.finsight.auth.service.impl;

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
import com.akademi.finsight.auth.exception.LoginLimitException;
import com.akademi.finsight.auth.otp.exception.OtpLimitException;
import com.akademi.finsight.auth.passwordhistory.service.PasswordHistoryService;
import com.akademi.finsight.auth.passwordreset.service.PasswordResetTokenService;
import com.akademi.finsight.auth.ratelimiter.config.LoginRateLimitProperties;
import com.akademi.finsight.auth.ratelimiter.service.LoginRateLimitService;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResponse;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResult;
import com.akademi.finsight.auth.refreshtoken.service.RefreshTokenService;
import com.akademi.finsight.auth.service.AuthService;
import com.akademi.finsight.auth.verificationtoken.service.VerificationTokenService;
import com.akademi.finsight.monitoring.AppMetrics;
import com.akademi.finsight.notification.model.NotificationType;
import com.akademi.finsight.notification.service.NotificationService;
import com.akademi.finsight.auth.otp.service.OtpService;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.security.jwt.service.JwtService;
import com.akademi.finsight.security.jwt.service.TokenInvalidationService;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.exception.UserException;
import com.akademi.finsight.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenInvalidationService tokenInvalidationService;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final LoginRateLimitService loginRateLimitService;
    private final VerificationTokenService verificationTokenService;
    private final OtpService otpService;
    private final NotificationService notificationService;
    private final LoginRateLimitProperties loginRateLimitProperties;
    private final PasswordResetTokenService passwordResetTokenService;
    private final PasswordHistoryService passwordHistoryService;
    private final AuditLogService auditLogService;
    private final AppMetrics appMetrics;

    @Override
    @Transactional(noRollbackFor = LoginLimitException.class)
    public LoginResult login(LoginRequest request) {
        User user = authenticateUser(request);
        loginRateLimitService.resetAttempts(request.identifier());

        if (!user.isEmailVerified()) {
            log.warn("Login rejected, email not verified: event=EMAIL_NOT_VERIFIED, email={}", MaskType.EMAIL.mask(user.getEmail()));
            throw new AuthException(AuthErrorType.EMAIL_NOT_VERIFIED);
        }

        userService.updateLastLogin(user);

        if (user.isFirstLogin()) {
            appMetrics.incrementLoginSuccess();

            log.info("First login, OTP bypassed: event=USER_FIRST_LOGIN, email={}", MaskType.EMAIL.mask(user.getEmail()));

            auditLogService.createAuditLogForSelf(AuditActionType.LOGIN_SUCCESS, user);
            return authenticateAndGenerateTokens(user);
        }

        appMetrics.incrementOtpSend();

        otpService.generateOtp(user.getEmail(), user.getFirstName());
        log.info("OTP sent for 2FA: event=OTP_REQUIRED, email={}", MaskType.EMAIL.mask(user.getEmail()));

        return new LoginResult.OtpRequired("OTP code sent to your email address.");
    }

    private User authenticateUser(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));
        } catch (AuthenticationException exception) {
            appMetrics.incrementLoginFailure();
            boolean blocked = loginRateLimitService.incrementFailedAttempts(request.identifier());
            if (blocked) {
                appMetrics.incrementAccountLocked();

                sendAccountLockedNotification(request.identifier());
                throw new LoginLimitException();
            }
            throw exception;
        }

        if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            appMetrics.incrementLoginFailure();

            loginRateLimitService.incrementFailedAttempts(request.identifier());
            throw new AuthException(AuthErrorType.INVALID_CREDENTIALS);
        }

        return userService.findByEmail(userDetails.getUsername());
    }

    private void sendAccountLockedNotification(String identifier) {
        try {
            User user = userService.findByIdentifier(identifier);
            long blockMinutes = loginRateLimitProperties.getBlockDuration().toMinutes();
            String resetUrl = passwordResetTokenService.createResetUrl(user);

            Map<String, String> params = Map.of(
                    "firstName", user.getFirstName(),
                    "maxAttempts", String.valueOf(loginRateLimitProperties.getMaxAttempts()),
                    "blockMinutes", String.valueOf(blockMinutes),
                    "resetUrl", resetUrl
            );

            notificationService.notify(NotificationType.ACCOUNT_LOCKED_EMAIL, user.getEmail(), params);

            log.info("Account locked notification sent: email={}", MaskType.EMAIL.mask(user.getEmail()));
        } catch (Exception e) {
            log.warn("Failed to send account locked notification for identifier: {}", identifier);
        }
    }

    /** Sifre degisiminden kullanici haberdar edilmezse ele gecirilen hesap fark edilmeden kalir. */
    private void sendPasswordChangedNotification(User user) {
        try {
            String resetUrl = passwordResetTokenService.createResetUrl(user);

            notificationService.notify(
                    NotificationType.PASSWORD_CHANGED_EMAIL,
                    user.getEmail(),
                    Map.of("firstName", user.getFirstName(), "resetUrl", resetUrl)
            );

            log.info("Password changed notification sent: email={}", MaskType.EMAIL.mask(user.getEmail()));
        } catch (Exception exception) {
            log.warn("Failed to send password changed notification: email={}", MaskType.EMAIL.mask(user.getEmail()), exception);
        }
    }

    private LoginResult.Authenticated authenticateAndGenerateTokens(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails, user.isFirstLogin());
        RefreshTokenResult result = refreshTokenService.createAndSave(user);

        return new LoginResult.Authenticated(
                accessToken, result.rawToken(),
                jwtService.getAccessTokenExpiryMinutes(), user.isFirstLogin());
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshTokens(RefreshTokenRequest request) {
        appMetrics.incrementTokenRefresh();

        RefreshTokenResult result = refreshTokenService.rotateToken(request);

        User user = result.refreshToken().getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails, user.isFirstLogin());

        return new RefreshTokenResponse(accessToken, result.rawToken(), jwtService.getAccessTokenExpiryMinutes());
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revokeToken(request);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, String email) {
        User user = userService.findByEmail(email);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            log.warn("Password change failed: event=WRONG_CURRENT_PASSWORD, email={}", MaskType.EMAIL.mask(user.getEmail()));
            throw new AuthException(AuthErrorType.WRONG_CURRENT_PASSWORD);
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            log.info("Password change rejected: event=SAME_PASSWORD, email={}", MaskType.EMAIL.mask(user.getEmail()));
            throw new AuthException(AuthErrorType.SAME_PASSWORD);
        }

        passwordHistoryService.assertNotRecentlyUsed(user, request.newPassword());

        String encodedPassword = passwordEncoder.encode(request.newPassword());
        userService.updatePassword(user, encodedPassword, user.isFirstLogin());
        passwordHistoryService.updatePasswordHistory(user, encodedPassword);
        refreshTokenService.revokeAllByUser(user);
        tokenInvalidationService.invalidateTokensIssuedBefore(user.getEmail());
        auditLogService.createAuditLogForSelf(AuditActionType.PASSWORD_CHANGED, user);
        sendPasswordChangedNotification(user);

        log.info("Password changed: event=PASSWORD_CHANGED, email={}", MaskType.EMAIL.mask(user.getEmail()));
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        verificationTokenService.verifyEmail(token);
    }

    @Override
    @Transactional(noRollbackFor = OtpLimitException.class)
    public LoginResult.Authenticated otpLogin(OtpLoginRequest request) {
        User user = userService.findByIdentifier(request.identifier());

        if (!otpService.validateActiveOtp(user.getEmail())) {
            appMetrics.incrementOtpVerifyFailure();

            log.warn("OTP login rejected, no active OTP: email={}", MaskType.EMAIL.mask(user.getEmail()));
            throw new AuthException(AuthErrorType.OTP_NOT_ELIGIBLE);
        }

        otpService.validateOtp(user.getEmail(), request.code());

        appMetrics.incrementOtpVerifySuccess();
        appMetrics.incrementLoginSuccess();

        log.info("OTP login successful: event=OTP_LOGIN_SUCCESS, email={}", MaskType.EMAIL.mask(user.getEmail()));
        auditLogService.createAuditLogForSelf(AuditActionType.LOGIN_SUCCESS, user);
        return authenticateAndGenerateTokens(user);
    }

    @Override
    public void resendOtp(ResendOtpRequest request) {
        User user = userService.findByIdentifier(request.identifier());

        if (!otpService.validateActiveOtp(user.getEmail())) {
            log.warn("OTP resend rejected, no active OTP: email={}", MaskType.EMAIL.mask(user.getEmail()));
            throw new AuthException(AuthErrorType.OTP_NOT_ELIGIBLE);
        }

        appMetrics.incrementOtpSend();
        otpService.generateOtp(user.getEmail(), user.getFirstName());
        log.info("OTP resent: event=OTP_RESENT, email={}", MaskType.EMAIL.mask(user.getEmail()));
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        try {
            User user = userService.findByEmail(request.email());
            if (!user.isEmailVerified()) {
                log.warn("Password reset rejected, email not verified: event=EMAIL_NOT_VERIFIED, email={}", MaskType.EMAIL.mask(request.email()));
                return;
            }
            appMetrics.incrementPasswordResetRequest();
            passwordResetTokenService.createAndSendResetToken(user);
            log.info("Password reset requested: event=PASSWORD_RESET_REQUESTED, email={}", MaskType.EMAIL.mask(request.email()));
        } catch (UserException e) {
            log.info("Password reset requested for unknown email: event=PASSWORD_RESET_UNKNOWN_EMAIL, email={}", MaskType.EMAIL.mask(request.email()));
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = passwordResetTokenService.consumeToken(request.token());

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            log.info("Password reset rejected: event=SAME_PASSWORD, email={}", MaskType.EMAIL.mask(user.getEmail()));
            throw new AuthException(AuthErrorType.SAME_PASSWORD);
        }

        passwordHistoryService.assertNotRecentlyUsed(user, request.newPassword());

        String encodedPassword = passwordEncoder.encode(request.newPassword());
        userService.updatePassword(user, encodedPassword, true);
        passwordHistoryService.updatePasswordHistory(user, encodedPassword);
        refreshTokenService.revokeAllByUser(user);
        tokenInvalidationService.invalidateTokensIssuedBefore(user.getEmail());
        auditLogService.createAuditLogForSelf(AuditActionType.PASSWORD_RESET_COMPLETED, user);
        loginRateLimitService.clearAllRestrictions(user.getEmail());
        loginRateLimitService.clearAllRestrictions(user.getUsername());
        sendPasswordChangedNotification(user);

        log.info("Password reset completed: event=PASSWORD_RESET_COMPLETED, email={}", MaskType.EMAIL.mask(user.getEmail()));
    }
}
