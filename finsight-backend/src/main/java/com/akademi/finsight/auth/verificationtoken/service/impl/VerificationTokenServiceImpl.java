package com.akademi.finsight.auth.verificationtoken.service.impl;

import com.akademi.finsight.auth.verificationtoken.exception.VerificationTokenErrorType;
import com.akademi.finsight.auth.verificationtoken.exception.VerificationTokenException;
import com.akademi.finsight.auth.verificationtoken.dto.VerificationTokenRequest;
import com.akademi.finsight.auth.verificationtoken.entity.VerificationToken;
import com.akademi.finsight.auth.verificationtoken.repository.VerificationTokenRepository;
import com.akademi.finsight.auth.verificationtoken.service.VerificationTokenService;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.notification.service.EmailService;
import com.akademi.finsight.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository repository;
    private final EmailService emailService;

    @Value("${app.verification.url}")
    private String verificationBaseUrl;


    public void createAndSendVerificationToken(User user, String temporaryPassword) {
        repository.deleteByUserId(user.getId());
        repository.flush();

        String token = UUID.randomUUID().toString();
        String hash = passwordEncoder.encode(token);
        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(hash)
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        repository.save(verificationToken);

        String verificationUrl = verificationBaseUrl + token;
        VerificationTokenRequest tokenRequest = new VerificationTokenRequest(user.getUsername(), user.getEmail(), temporaryPassword, verificationUrl);

        log.info("Verification mail is being sent to {}", MaskType.EMAIL.mask(user.getEmail()));

        emailService.sendVerificationEmail(tokenRequest, LocaleContextHolder.getLocale());
    }

    @Override
    public void verifyEmail(String token) {
        VerificationToken verificationToken = repository.findAllByExpiresAtAfter(Instant.now())
                .stream()
                .filter(vt -> passwordEncoder.matches(token, vt.getToken()))
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


