package com.akademi.finsight.auth.verificationtoken.service.impl;

import com.akademi.finsight.auth.verificationtoken.exception.VerificationTokenErrorType;
import com.akademi.finsight.auth.verificationtoken.exception.VerificationTokenException;
import com.akademi.finsight.auth.verificationtoken.entity.VerificationToken;
import com.akademi.finsight.auth.verificationtoken.repository.VerificationTokenRepository;
import com.akademi.finsight.auth.verificationtoken.service.VerificationTokenService;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.notification.model.NotificationType;
import com.akademi.finsight.notification.service.NotificationService;
import com.akademi.finsight.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository repository;
    private final NotificationService notificationService;

    @Value("${app.verification.url}")
    private String verificationBaseUrl;

    @Value("${app.verification.expire-days}")
    private long verificationExpireDays;


    public void createAndSendVerificationToken(User user, String temporaryPassword) {
        generateTokenAndSendEmail(user, temporaryPassword);
        log.info("Verification mail sent: event=VERIFICATION_CREATED, email={}", MaskType.EMAIL.mask(user.getEmail()));
    }

    @Override
    public void resendVerificationToken(User user, String temporaryPassword) {
        repository.deleteByUserId(user.getId());
        repository.flush();

        generateTokenAndSendEmail(user, temporaryPassword);
        log.info("Verification mail resent: event=VERIFICATION_RESENT, email={}", MaskType.EMAIL.mask(user.getEmail()));
    }

    private void generateTokenAndSendEmail(User user, String temporaryPassword) {

        String token = UUID.randomUUID().toString();
        String hash = passwordEncoder.encode(token);
        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(hash)
                .expiresAt(Instant.now().plus(verificationExpireDays, ChronoUnit.DAYS))
                .build();

        repository.save(verificationToken);

        String verificationUrl = verificationBaseUrl + token;

        notificationService.notify(
                NotificationType.VERIFICATION_EMAIL,
                user.getEmail(),
                Map.of("username", user.getUsername(),
                       "temporaryPassword", temporaryPassword,
                       "verificationUrl", verificationUrl)
        );
    }

    @Override
    public void verifyEmail(String token) {
        VerificationToken verificationToken = repository.findAllByExpiresAtAfter(Instant.now())
                .stream()
                .filter(verificationTokenEntity -> passwordEncoder.matches(token, verificationTokenEntity.getToken()))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Email verification failed: event=VERIFICATION_TOKEN_INVALID");
                    return new VerificationTokenException(VerificationTokenErrorType.VERIFICATION_TOKEN_INVALID);
                });

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEnabled(true);

        repository.delete(verificationToken);
        log.info("Email verified: event=EMAIL_VERIFIED, email={}", MaskType.EMAIL.mask(user.getEmail()));
    }

    @Override
    public int deleteExpiredTokens() {
        return repository.deleteExpiredTokens(Instant.now());
    }
}


