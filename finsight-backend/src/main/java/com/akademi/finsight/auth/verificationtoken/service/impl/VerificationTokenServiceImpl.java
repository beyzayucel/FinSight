package com.akademi.finsight.auth.verificationtoken.service.impl;

import com.akademi.finsight.auth.verificationtoken.dto.VerificationTokenRequest;
import com.akademi.finsight.auth.verificationtoken.entity.VerificationToken;
import com.akademi.finsight.auth.verificationtoken.mapper.VerificationTokenMapper;
import com.akademi.finsight.auth.verificationtoken.repository.VerificationTokenRepository;
import com.akademi.finsight.auth.verificationtoken.service.VerificationTokenService;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public static final String VERIFICATION_URL = "http://localhost:8080/api/auth/verify?token=";

    private final VerificationTokenMapper tokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository repository;


    public void createAndSendVerificationToken(User user, String temporaryPassword) {
        repository.deleteByUserId(user.getId());
        repository.flush();

        String token = UUID.randomUUID().toString();
        String hash = passwordEncoder.encode(token);
        VerificationToken verificationToken = tokenMapper.toEntity(user, hash);
        verificationToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
        repository.save(verificationToken);

        String verificationUrl = VERIFICATION_URL + token;
        VerificationTokenRequest tokenRequest = new VerificationTokenRequest(user.getUsername(), user.getEmail(), temporaryPassword, verificationUrl);
        log.info("Verification mail is being sent to {}", MaskType.EMAIL.mask(user.getEmail()));

//        emailService.sendVerificationEmail(tokenRequest, LocaleContextHolder.getLocale()); devamına mehmet kafka entegre edilecek
    }

    @Override
    public int deleteExpiredTokens() {
        return repository.deleteExpiredTokens(Instant.now());
    }
}


