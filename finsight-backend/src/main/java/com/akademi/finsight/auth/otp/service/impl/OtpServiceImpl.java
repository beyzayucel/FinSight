package com.akademi.finsight.auth.otp.service.impl;

import com.akademi.finsight.auth.passwordreset.service.PasswordResetTokenService;
import com.akademi.finsight.auth.ratelimiter.service.LoginBlocklistService;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.service.UserService;
import com.akademi.finsight.notification.exception.NotificationPublishException;
import com.akademi.finsight.notification.model.NotificationType;
import com.akademi.finsight.notification.service.NotificationService;
import com.akademi.finsight.auth.otp.config.OtpProperties;
import com.akademi.finsight.auth.otp.exception.OtpErrorType;
import com.akademi.finsight.auth.otp.exception.OtpException;
import com.akademi.finsight.auth.otp.exception.OtpLimitException;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.auth.otp.keygenerator.OtpKeyGenerator;
import com.akademi.finsight.auth.otp.service.OtpService;
import com.akademi.finsight.auth.ratelimiter.util.IdentifierHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;
    private final OtpKeyGenerator otpKeyGenerator;
    private final NotificationService notificationService;
    private final OtpProperties otpProperties;
    private final LoginBlocklistService loginBlocklistService;
    private final IdentifierHasher identifierHasher;
    private final PasswordResetTokenService passwordResetTokenService;
    private final UserService userService;

    private final SecureRandom secureRandom = new SecureRandom();



    @Override
    public void generateOtp(String email, String firstName) {
        validateEmailInput(email);
        log.info("OTP generation request received: email={}", MaskType.EMAIL.mask(email));

        checkCooldown(email);
        clearAllOtpKeys(email);

        String otpCode = generateOtpCode();
        saveOtpToRedis(email, otpCode);

        try {
            sendOtpNotification(email, firstName, otpCode);
        } catch (NotificationPublishException e) {
            log.error("Failed to send OTP notification: email={}", MaskType.EMAIL.mask(email));
            invalidateOtp(email);
            throw new OtpException(OtpErrorType.OTP_SEND_FAILED, e);
        }

        log.info("OTP generated and dispatched: email={}", MaskType.EMAIL.mask(email));
    }

    private void checkCooldown(String email) {
        String cooldownKey = otpKeyGenerator.generateCooldownKey(email);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            Long expire = redisTemplate.getExpire(cooldownKey);
            Long remaining = (expire != null && expire > 0) ? expire : otpProperties.getCooldownSeconds();
            log.warn("OTP generation rejected, cooldown active: email={}, remaining={}s", MaskType.EMAIL.mask(email), remaining);
            throw new OtpException(OtpErrorType.OTP_COOLDOWN_ACTIVE, remaining);
        }
    }

    @Override
    public void validateOtp(String email, String inputCode) {
        validateEmailInput(email);
        log.info("OTP verification request received: email={}", MaskType.EMAIL.mask(email));

        checkMaxAttempts(email);
        String storedOtpCode = redisTemplate.opsForValue()
                .get(otpKeyGenerator.generateCodeKey(email));
        verifyOtpCode(storedOtpCode, inputCode, email);

        clearAllOtpKeys(email);
        log.info("OTP verification successful: email={}", MaskType.EMAIL.mask(email));
    }

    private void checkMaxAttempts(String email) {
        String attemptsKey = otpKeyGenerator.generateAttemptsKey(email);
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        if (attemptsStr != null && Long.parseLong(attemptsStr) >= otpProperties.getMaxAttempts()) {
            invalidateOtp(email);
            log.warn("OTP max attempts already exceeded: email={}", MaskType.EMAIL.mask(email));
            throw new OtpException(OtpErrorType.OTP_MAX_ATTEMPTS_EXCEEDED);
        }
    }

    private void verifyOtpCode(String storedOtpCode, String inputCode, String email) {
        if (storedOtpCode == null) {
            log.warn("OTP verification failed, code expired or invalid: email={}", MaskType.EMAIL.mask(email));
            throw new OtpException(OtpErrorType.OTP_EXPIRED_OR_INVALID);
        }

        if (!storedOtpCode.equals(inputCode)) {
            handleFailedAttempt(email);
        }
    }

    private void handleFailedAttempt(String email) {
        String attemptsKey = otpKeyGenerator.generateAttemptsKey(email);
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts == 1L) {
            redisTemplate.expire(attemptsKey, otpProperties.getExpireDuration());
        }
        int remaining = otpProperties.getMaxAttempts() - attempts.intValue();

        if (attempts >= otpProperties.getMaxAttempts()) {
            invalidateOtp(email);
            incrementAbuseCycle(email);
            log.warn("OTP max attempts exceeded, OTP invalidated: email={}, attempt={}/{}", MaskType.EMAIL.mask(email), attempts, otpProperties.getMaxAttempts());
            throw new OtpException(OtpErrorType.OTP_MAX_ATTEMPTS_EXCEEDED);
        }
        log.warn("OTP verification failed, incorrect code: email={}, attempt={}/{}", MaskType.EMAIL.mask(email), attempts, otpProperties.getMaxAttempts());
        throw new OtpException(OtpErrorType.OTP_INCORRECT, remaining);
    }

    private void incrementAbuseCycle(String email) {
        OtpProperties.Abuse abuseConfig = otpProperties.getAbuse();
        String abuseKey = otpKeyGenerator.generateAbuseKey(email);

        Long cycles = redisTemplate.opsForValue().increment(abuseKey);
        if (cycles == 1L) {
            redisTemplate.expire(abuseKey, abuseConfig.getWindowDuration());
        }

        log.warn("OTP abuse cycle incremented: email={}, cycle={}/{}", MaskType.EMAIL.mask(email), cycles, abuseConfig.getMaxCycles());

        if (cycles >= abuseConfig.getMaxCycles()) {
            String hashedEmail = identifierHasher.hash(email);
            loginBlocklistService.blockUser(hashedEmail, abuseConfig.getBlockDuration());
            redisTemplate.delete(abuseKey);
            sendOtpAbuseNotification(email);
            log.warn("OTP abuse detected, account blocked: email={}", MaskType.EMAIL.mask(email));
            throw new OtpLimitException();
        }
    }

    private void sendOtpAbuseNotification(String email) {
        try {
            User user = userService.findByEmail(email);
            String resetUrl = passwordResetTokenService.createResetUrl(user);

            Map<String, String> params = Map.of(
                    "blockMinutes", String.valueOf(otpProperties.getAbuse().getBlockDuration().toMinutes()),
                    "resetUrl", resetUrl
            );

            notificationService.notify(NotificationType.OTP_ABUSE_LOCKED_EMAIL, email, params);
        } catch (Exception e) {
            log.warn("Failed to send OTP abuse notification: email={}", MaskType.EMAIL.mask(email));
        }
    }

    @Override
    public boolean validateActiveOtp(String email) {
        checkMaxAttempts(email);
        boolean active = Boolean.TRUE.equals(redisTemplate.hasKey(otpKeyGenerator.generateCodeKey(email)));
        log.debug("OTP active check: email={}, active={}", MaskType.EMAIL.mask(email), active);
        return active;
    }

    private String generateOtpCode() {
        int otpInt = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otpInt);
    }

    private void invalidateOtp(String email) {
        redisTemplate.delete(otpKeyGenerator.generateCodeKey(email));
        redisTemplate.delete(otpKeyGenerator.generateCooldownKey(email));
    }

    private void clearAllOtpKeys(String email) {
        redisTemplate.delete(otpKeyGenerator.generateCodeKey(email));
        redisTemplate.delete(otpKeyGenerator.generateCooldownKey(email));
        redisTemplate.delete(otpKeyGenerator.generateAttemptsKey(email));
    }

    private void saveOtpToRedis(String email, String otpCode) {
        String otpKey = otpKeyGenerator.generateCodeKey(email);
        String cooldownKey = otpKeyGenerator.generateCooldownKey(email);
        redisTemplate.opsForValue().set(otpKey, otpCode, otpProperties.getExpireDuration());
        redisTemplate.opsForValue().set(cooldownKey, "1", otpProperties.getCooldownDuration());
        log.debug("OTP and cooldown keys saved to Redis.");
    }

    private void sendOtpNotification(String email, String firstName, String otpCode) {
        Map<String, String> params = Map.of(
                "firstName", firstName,
                "otpCode", otpCode,
                "expireMinutes", String.valueOf(otpProperties.getExpireDuration().toMinutes())
        );

        notificationService.notify(NotificationType.OTP_EMAIL, email, params);
    }


    private void validateEmailInput(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be empty.");
        }
    }


}
