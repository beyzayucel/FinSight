package com.akademi.finsight.auth.refreshtoken.service.impl;


import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResult;
import com.akademi.finsight.auth.refreshtoken.entity.RefreshToken;
import com.akademi.finsight.auth.refreshtoken.exception.RefreshTokenErrorType;
import com.akademi.finsight.auth.refreshtoken.exception.RefreshTokenException;
import com.akademi.finsight.auth.refreshtoken.repository.RefreshTokenRepository;
import com.akademi.finsight.auth.refreshtoken.service.RefreshTokenService;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.security.jwt.config.JwtProperties;
import com.akademi.finsight.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public RefreshTokenResult createAndSave(User user) {
        String rawToken = generateSecureToken();
        RefreshToken refreshToken = buildToken(user, hashToken(rawToken));
        refreshTokenRepository.save(refreshToken);
        return new RefreshTokenResult(rawToken, refreshToken);
    }

    @Override
    @Transactional
    public RefreshTokenResult rotateToken(RefreshTokenRequest request) {
        RefreshToken existingToken = findByTokenOrThrow(request.refreshToken());
        validateToken(existingToken);

        markAsRevoked(existingToken);

        String rawToken = generateSecureToken();
        RefreshToken newToken = buildToken(existingToken.getUser(), hashToken(rawToken));
        refreshTokenRepository.save(newToken);

        log.info("Refresh token rotated: event=TOKEN_ROTATED, email={}", MaskType.EMAIL.mask(existingToken.getUser().getEmail()));

        return new RefreshTokenResult(rawToken, newToken);
    }

    @Override
    @Transactional
    public void revokeToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = findByTokenOrThrow(request.refreshToken());
        markAsRevoked(refreshToken);
        log.info("User logged out: event=USER_LOGGED_OUT, email={}", MaskType.EMAIL.mask(refreshToken.getUser().getEmail()));
    }

    @Override
    @Transactional
    public void revokeAllByUser(User user) {
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
        log.info("All tokens revoked: event=ALL_TOKENS_REVOKED, email={}", MaskType.EMAIL.mask(user.getEmail()));
    }

    private RefreshToken buildToken(User user, String tokenHash) {
        return RefreshToken.builder()
                .tokenHash(tokenHash)
                .expiryDate(Instant.now().plus(jwtProperties.getRefreshTokenExpiry()))
                .user(user)
                .build();
    }

    private RefreshToken findByTokenOrThrow(String rawToken) {
        String tokenHash = hashToken(rawToken);
        return refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RefreshTokenException(RefreshTokenErrorType.REFRESH_TOKEN_NOT_FOUND));
    }

    private static String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private void validateToken(RefreshToken token) {
        if (token.isRevoked()) {
            log.warn("Revoked refresh token reuse detected: event=TOKEN_REUSE, email={}", MaskType.EMAIL.mask(token.getUser().getEmail()));
            throw new RefreshTokenException(RefreshTokenErrorType.REFRESH_TOKEN_REVOKED);
        }
        if (token.isExpired()) {
            log.debug("Expired refresh token used: event=TOKEN_EXPIRED, email={}", MaskType.EMAIL.mask(token.getUser().getEmail()));
            throw new RefreshTokenException(RefreshTokenErrorType.REFRESH_TOKEN_EXPIRED);
        }
    }

    private void markAsRevoked(RefreshToken token) {
        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
    }
}
