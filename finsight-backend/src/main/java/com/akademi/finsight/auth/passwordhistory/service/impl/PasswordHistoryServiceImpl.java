package com.akademi.finsight.auth.passwordhistory.service.impl;

import com.akademi.finsight.auth.exception.AuthErrorType;
import com.akademi.finsight.auth.exception.AuthException;
import com.akademi.finsight.auth.passwordhistory.config.PasswordHistoryProperties;
import com.akademi.finsight.auth.passwordhistory.entity.PasswordHistory;
import com.akademi.finsight.auth.passwordhistory.repository.PasswordHistoryRepository;
import com.akademi.finsight.auth.passwordhistory.service.PasswordHistoryService;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PasswordHistoryServiceImpl implements PasswordHistoryService {

    private final PasswordHistoryRepository repository;
    private final PasswordHistoryProperties properties;
    private final PasswordEncoder passwordEncoder;

    /** Hash'ler tuzlu oldugu icin esitlikle aranamaz, her kayit tek tek karsilastirilir. */
    @Override
    @Transactional(readOnly = true)
    public void assertNotRecentlyUsed(User user, String rawPassword) {
        boolean reused = repository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), Limit.of(properties.getSize()))
                .stream()
                .anyMatch(history -> passwordEncoder.matches(rawPassword, history.getPasswordHash()));

        if (reused) {
            log.info("Password change rejected: event=PASSWORD_RECENTLY_USED, email={}",
                    MaskType.EMAIL.mask(user.getEmail()));
            throw new AuthException(AuthErrorType.PASSWORD_RECENTLY_USED);
        }
    }

    @Override
    public void updatePasswordHistory(User user, String encodedPassword) {
        repository.save(PasswordHistory.builder()
                .user(user)
                .passwordHash(encodedPassword)
                .build());

        repository.deleteOlderThanNewest(user.getId(), properties.getSize());
    }
}
