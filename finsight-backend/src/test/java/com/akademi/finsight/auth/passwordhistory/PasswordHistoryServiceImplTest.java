package com.akademi.finsight.auth.passwordhistory;

import com.akademi.finsight.auth.exception.AuthErrorType;
import com.akademi.finsight.auth.exception.AuthException;
import com.akademi.finsight.auth.passwordhistory.config.PasswordHistoryProperties;
import com.akademi.finsight.auth.passwordhistory.entity.PasswordHistory;
import com.akademi.finsight.auth.passwordhistory.repository.PasswordHistoryRepository;
import com.akademi.finsight.auth.passwordhistory.service.impl.PasswordHistoryServiceImpl;
import com.akademi.finsight.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordHistoryServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final int HISTORY_SIZE = 3;
    private static final String RAW_PASSWORD = "YeniSifre1!";

    @Mock
    private PasswordHistoryRepository repository;

    @Mock
    private PasswordHistoryProperties properties;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordHistoryServiceImpl passwordHistoryService;

    @BeforeEach
    void setUp() {
        passwordHistoryService = new PasswordHistoryServiceImpl(repository, properties, passwordEncoder);
        lenient().when(properties.getSize()).thenReturn(HISTORY_SIZE);
    }

    // ──────────────────────────────────────────────────────────────
    // assertNotRecentlyUsed
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("assertNotRecentlyUsed")
    class AssertNotRecentlyUsed {

        @Test
        @DisplayName("should pass when the password matches none of the recent ones")
        void shouldPassWhenPasswordIsNew() {
            when(repository.findByUserIdOrderByCreatedAtDesc(eq(USER_ID), any(Limit.class)))
                    .thenReturn(List.of(historyEntry("hash-1"), historyEntry("hash-2")));
            when(passwordEncoder.matches(eq(RAW_PASSWORD), anyString())).thenReturn(false);

            assertDoesNotThrow(() -> passwordHistoryService.assertNotRecentlyUsed(createUser(), RAW_PASSWORD));
        }

        @Test
        @DisplayName("should throw PASSWORD_RECENTLY_USED when the password was used before")
        void shouldThrowWhenPasswordWasRecentlyUsed() {
            when(repository.findByUserIdOrderByCreatedAtDesc(eq(USER_ID), any(Limit.class)))
                    .thenReturn(List.of(historyEntry("hash-1")));
            when(passwordEncoder.matches(RAW_PASSWORD, "hash-1")).thenReturn(true);

            User user = createUser();

            AuthException exception = assertThrows(
                    AuthException.class,
                    () -> passwordHistoryService.assertNotRecentlyUsed(
                            user,
                            RAW_PASSWORD
                    )
            );

            assertEquals(AuthErrorType.PASSWORD_RECENTLY_USED, exception.getErrorType());
        }

        /** Hash'ler tuzlu oldugu icin esitlikle aranamaz; her kayit encoder ile karsilastirilmali. */
        @Test
        @DisplayName("should compare against every recent entry, not just the newest")
        void shouldCompareAgainstEveryRecentEntry() {
            when(repository.findByUserIdOrderByCreatedAtDesc(eq(USER_ID), any(Limit.class)))
                    .thenReturn(List.of(historyEntry("hash-1"), historyEntry("hash-2"), historyEntry("hash-3")));
            when(passwordEncoder.matches(RAW_PASSWORD, "hash-1")).thenReturn(false);
            when(passwordEncoder.matches(RAW_PASSWORD, "hash-2")).thenReturn(false);
            when(passwordEncoder.matches(RAW_PASSWORD, "hash-3")).thenReturn(true);

            User user = createUser();

            assertThrows(
                    AuthException.class,
                    () -> passwordHistoryService.assertNotRecentlyUsed(
                            user,
                            RAW_PASSWORD
                    )
            );
        }

        @Test
        @DisplayName("should ask the repository for exactly the configured number of entries")
        void shouldLimitLookupToConfiguredSize() {
            when(repository.findByUserIdOrderByCreatedAtDesc(eq(USER_ID), any(Limit.class))).thenReturn(List.of());

            passwordHistoryService.assertNotRecentlyUsed(createUser(), RAW_PASSWORD);

            ArgumentCaptor<Limit> limitCaptor = ArgumentCaptor.forClass(Limit.class);
            verify(repository).findByUserIdOrderByCreatedAtDesc(eq(USER_ID), limitCaptor.capture());
            assertEquals(HISTORY_SIZE, limitCaptor.getValue().max());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // record
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("record")
    class Record {

        @Test
        @DisplayName("should store the encoded password against the user")
        void shouldStoreEncodedPassword() {
            User user = createUser();

            passwordHistoryService.updatePasswordHistory(user, "encoded-new-password");

            ArgumentCaptor<PasswordHistory> captor = ArgumentCaptor.forClass(PasswordHistory.class);
            verify(repository).save(captor.capture());
            assertEquals("encoded-new-password", captor.getValue().getPasswordHash());
            assertEquals(user, captor.getValue().getUser());
        }

        /** Budama yapilmazsa tablo kullanici basina sinirsiz buyur. */
        @Test
        @DisplayName("should prune entries beyond the configured size")
        void shouldPruneOlderEntries() {
            passwordHistoryService.updatePasswordHistory(createUser(), "encoded-new-password");

            verify(repository).deleteOlderThanNewest(USER_ID, HISTORY_SIZE);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────

    private User createUser() {
        return User.builder()
                .id(USER_ID)
                .email("mehmet@test.com")
                .username("mehmet")
                .password("encodedPassword")
                .firstName("Mehmet")
                .lastName("Test")
                .emailVerified(true)
                .build();
    }

    private PasswordHistory historyEntry(String hash) {
        return PasswordHistory.builder().passwordHash(hash).build();
    }
}
