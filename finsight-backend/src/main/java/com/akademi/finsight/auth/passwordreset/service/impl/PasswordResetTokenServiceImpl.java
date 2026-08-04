package com.akademi.finsight.auth.passwordreset.service.impl;

import com.akademi.finsight.auth.passwordreset.config.PasswordResetProperties;
import com.akademi.finsight.auth.passwordreset.dto.PasswordResetEmailRequest;
import com.akademi.finsight.auth.passwordreset.entity.PasswordResetToken;
import com.akademi.finsight.auth.passwordreset.exception.PasswordResetErrorType;
import com.akademi.finsight.auth.passwordreset.exception.PasswordResetException;
import com.akademi.finsight.auth.passwordreset.repository.PasswordResetTokenRepository;
import com.akademi.finsight.auth.passwordreset.service.PasswordResetTokenService;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.notification.service.EmailService;
import com.akademi.finsight.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository repository;
    private final EmailService emailService;
    private final PasswordResetProperties properties;

    @Override
    public void createAndSendResetToken(User user) {
        repository.deleteByUserId(user.getId());
        repository.flush();

        String token = UUID.randomUUID().toString();
        String hash = passwordEncoder.encode(token);
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(hash)
                .expiresAt(Instant.now().plus(properties.getExpireDuration()))
                .build();

        repository.save(resetToken);

        String resetUrl = properties.getUrl() + token;
        PasswordResetEmailRequest emailRequest = new PasswordResetEmailRequest(user.getFirstName(), user.getEmail(), resetUrl);
        log.info("Password reset mail is being sent to {}", MaskType.EMAIL.mask(user.getEmail()));

        emailService.sendPasswordResetEmail(emailRequest, LocaleContextHolder.getLocale());
    }

    @Override
    public User consumeToken(String token) {
        PasswordResetToken resetToken = repository.findAllByExpiresAtAfter(Instant.now())
                .stream()
                .filter(entity -> passwordEncoder.matches(token, entity.getToken()))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Password reset failed: event=PASSWORD_RESET_TOKEN_INVALID");
                    return new PasswordResetException(PasswordResetErrorType.PASSWORD_RESET_TOKEN_INVALID);
                });

        User user = resetToken.getUser();
        repository.delete(resetToken);
        log.info("Password reset token consumed: event=PASSWORD_RESET_TOKEN_CONSUMED, email={}", MaskType.EMAIL.mask(user.getEmail()));

        return user;
    }

    @Override
    public int deleteExpiredTokens() {
        return repository.deleteExpiredTokens(Instant.now());
    }
}
