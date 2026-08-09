package com.akademi.finsight.auth.passwordreset.service.impl;

import com.akademi.finsight.auth.passwordreset.config.PasswordResetProperties;
import com.akademi.finsight.auth.passwordreset.entity.PasswordResetToken;
import com.akademi.finsight.auth.passwordreset.exception.PasswordResetErrorType;
import com.akademi.finsight.auth.passwordreset.exception.PasswordResetException;
import com.akademi.finsight.auth.passwordreset.repository.PasswordResetTokenRepository;
import com.akademi.finsight.auth.passwordreset.service.PasswordResetTokenService;
import com.akademi.finsight.auth.ratelimiter.util.IdentifierHasher;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.notification.model.NotificationType;
import com.akademi.finsight.notification.service.NotificationService;
import com.akademi.finsight.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final IdentifierHasher tokenHasher;
    private final PasswordResetTokenRepository repository;
    private final NotificationService notificationService;
    private final PasswordResetProperties properties;

    @Transactional
    @Override
    public void createAndSendResetToken(User user) {
        String resetUrl = createTokenAndBuildUrl(user);
        log.info("Password reset mail is being sent to {}", MaskType.EMAIL.mask(user.getEmail()));

        notificationService.notify(
                NotificationType.PASSWORD_RESET_EMAIL,
                user.getEmail(),
                Map.of("firstName", user.getFirstName(), "resetUrl", resetUrl)
        );
    }

    @Transactional
    @Override
    public String createResetUrl(User user) {
        return createTokenAndBuildUrl(user);
    }

    private String createTokenAndBuildUrl(User user) {
        repository.deleteByUserId(user.getId());
        repository.flush();

        String token = UUID.randomUUID().toString();
        String hash = tokenHasher.hash(token);
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(hash)
                .expiresAt(Instant.now().plus(properties.getExpireDuration()))
                .build();

        repository.save(resetToken);
        return properties.getUrl() + token;
    }

    @Transactional
    @Override
    public User consumeToken(String token) {
        PasswordResetToken resetToken = repository.findByTokenAndExpiresAtAfter(tokenHasher.hash(token), Instant.now())
                .orElseThrow(() -> {
                    log.warn("Password reset failed: event=PASSWORD_RESET_TOKEN_INVALID");
                    return new PasswordResetException(PasswordResetErrorType.PASSWORD_RESET_TOKEN_INVALID);
                });

        User user = resetToken.getUser();
        repository.delete(resetToken);
        log.info("Password reset token consumed: event=PASSWORD_RESET_TOKEN_CONSUMED, email={}", MaskType.EMAIL.mask(user.getEmail()));

        return user;
    }

    @Transactional
    @Override
    public int deleteExpiredTokens() {
        return repository.deleteExpiredTokens(Instant.now());
    }
}
