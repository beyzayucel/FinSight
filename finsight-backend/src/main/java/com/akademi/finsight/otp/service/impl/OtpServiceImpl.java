package com.akademi.finsight.otp.service.impl;

import com.akademi.finsight.notification.exception.NotificationPublishException;
import com.akademi.finsight.notification.model.NotificationCommand;
import com.akademi.finsight.notification.model.NotificationType;
import com.akademi.finsight.notification.service.NotificationService;
import com.akademi.finsight.otp.config.OtpProperties;
import com.akademi.finsight.otp.exception.OtpSendException;
import com.akademi.finsight.otp.model.OtpGenerateResult;
import com.akademi.finsight.otp.keygenerator.OtpKeyGenerator;
import com.akademi.finsight.otp.model.OtpVerificationResult;
import com.akademi.finsight.otp.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.akademi.finsight.otp.constant.OtpMessageConstants.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();
    private final OtpKeyGenerator otpKeyGenerator;
    private final NotificationService notificationService;
    private final OtpProperties otpProperties;
    private final MessageSource messageSource;


    @Override
    public OtpGenerateResult generateOtp(String email, Locale locale) {

        validateEmailInput(email);
        log.info("OTP generation request received.");

        String cooldownKey = otpKeyGenerator.generateCooldownKey(email);
        String otpKey = otpKeyGenerator.generateCodeKey(email);

        Optional<OtpGenerateResult> cooldownResult = checkCooldown(cooldownKey);

        if (cooldownResult.isPresent()) {
            log.warn("OTP generation rejected, cooldown active.");
            return cooldownResult.get();
        }

        String otpCode = generateOtpCode();
        saveOtpToRedis(otpKey, cooldownKey, otpCode);

        try{
            sendOtpNotification(email, otpCode, locale);
        }catch (NotificationPublishException e){
            log.error("Failed to send OTP notification.");
            clearOtpKeysFromRedis(email, otpKey);
            throw new OtpSendException(e);
        }

        String successMessage = getMessage(GENERATE_SUCCESS);
        log.info("OTP generated successfully and dispatched to notification service.");

        return new OtpGenerateResult(true, successMessage, otpProperties.getCooldownSeconds());
    }

    @Override
    public OtpVerificationResult validateOtp(String email, String inputCode) {
        validateEmailInput(email);
        log.info("OTP verification request received.");

        String otpKey = otpKeyGenerator.generateCodeKey(email);
        Optional<String> storedOtpCode = Optional.ofNullable(redisTemplate.opsForValue().get(otpKey));

        Optional<OtpVerificationResult> validationError = validateStoredOtp(storedOtpCode, inputCode);
        if (validationError.isPresent()) {
            return validationError.get();
        }

        clearOtpKeysFromRedis(email, otpKey);
        log.info("OTP verification successful, Redis keys cleared.");
        return new OtpVerificationResult(true, getMessage(GENERATE_SUCCESS));
    }

    private String generateOtpCode(){
        int otpInt = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otpInt);
    }

    private void clearOtpKeysFromRedis(String email, String otpKey) {
        String cooldownKey = otpKeyGenerator.generateCooldownKey(email);
        redisTemplate.delete(otpKey);
        redisTemplate.delete(cooldownKey);
    }

    private Optional<OtpGenerateResult> checkCooldown(String cooldownKey) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            Long expire = redisTemplate.getExpire(cooldownKey);
            Long remaining = (expire != null && expire > 0) ? expire : otpProperties.getCooldownSeconds();
            return Optional.of(new OtpGenerateResult(false, getMessage(GENERATE_COOLDOWN), remaining));
        }
        return Optional.empty();
    }

    private void saveOtpToRedis(String otpKey, String cooldownKey, String otpCode) {
        redisTemplate.opsForValue().set(otpKey, otpCode, otpProperties.getExpireDuration());
        redisTemplate.opsForValue().set(cooldownKey, "1", otpProperties.getCooldownDuration());
        log.debug("OTP and cooldown keys saved to Redis.");
    }

    private void sendOtpNotification(String email, String otpCode, Locale locale) {
        Map<String, String> params = Map.of(
                "username", email,
                "otpCode", otpCode
        );

        String language = (locale != null) ? locale.getLanguage() : null;

        notificationService.notify(new NotificationCommand(
                NotificationType.OTP_EMAIL,
                null,
                email,
                params,
                language
        ));
    }

    private Optional<OtpVerificationResult> validateStoredOtp(Optional<String> storedOtpCode, String inputCode) {
        if (storedOtpCode.isEmpty()) {
            log.warn("OTP verification failed: Code is expired or invalid.");
            return Optional.of(new OtpVerificationResult(false, getMessage(VALIDATE_INVALID_OR_EXPIRED)));
        }

        if (!storedOtpCode.get().equals(inputCode)) {
            log.warn("OTP verification failed: Incorrect code provided.");
            return Optional.of(new OtpVerificationResult(false, getMessage(VALIDATE_INCORRECT)));
        }

        return Optional.empty();
    }

    private void validateEmailInput(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be empty.");
        }
    }

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }


}

