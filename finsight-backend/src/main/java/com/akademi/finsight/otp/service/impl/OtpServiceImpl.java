package com.akademi.finsight.otp.service.impl;

import com.akademi.finsight.notification.exception.NotificationPublishException;
import com.akademi.finsight.notification.model.NotificationCommand;
import com.akademi.finsight.notification.model.NotificationType;
import com.akademi.finsight.notification.service.NotificationService;
import com.akademi.finsight.otp.config.OtpProperties;
import com.akademi.finsight.otp.exception.OtpErrorType;
import com.akademi.finsight.otp.exception.OtpException;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.otp.keygenerator.OtpKeyGenerator;
import com.akademi.finsight.otp.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;
    private final OtpKeyGenerator otpKeyGenerator;
    private final NotificationService notificationService;
    private final OtpProperties otpProperties;

    private final SecureRandom secureRandom = new SecureRandom();



    @Override
    public void generateOtp(String email, String firstName, Locale locale) {
        validateEmailInput(email);
        log.info("OTP generation request received: email={}", MaskType.EMAIL.mask(email));

        checkCooldown(email);
        clearAllOtpKeys(email);

        String otpCode = generateOtpCode();
        saveOtpToRedis(email, otpCode);

        try {
            sendOtpNotification(email, firstName, otpCode, locale);
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
        if (attempts >= otpProperties.getMaxAttempts()) {
            invalidateOtp(email);
            log.warn("OTP max attempts exceeded, OTP invalidated: email={}, attempt={}/{}", MaskType.EMAIL.mask(email), attempts, otpProperties.getMaxAttempts());
            throw new OtpException(OtpErrorType.OTP_MAX_ATTEMPTS_EXCEEDED);
        }
        log.warn("OTP verification failed, incorrect code: email={}, attempt={}/{}", MaskType.EMAIL.mask(email), attempts, otpProperties.getMaxAttempts());
        throw new OtpException(OtpErrorType.OTP_INCORRECT);
    }



    @Override
    public boolean hasActiveOtp(String email) {
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

    private void sendOtpNotification(String email, String firstName, String otpCode, Locale locale) {
        Map<String, String> params = Map.of(
                "firstName", firstName,
                "otpCode", otpCode,
                "expireMinutes", String.valueOf(otpProperties.getExpireDuration().toMinutes())
        );

        String language = (locale != null) ? locale.getLanguage() : null;

        notificationService.notify(new NotificationCommand(
                NotificationType.OTP_EMAIL,
                email,
                params,
                language
        ));
    }


    private void validateEmailInput(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be empty.");
        }
    }


}
