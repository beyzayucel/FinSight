package com.akademi.finsight.auth.passwordreset.repository;

import com.akademi.finsight.auth.passwordreset.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenAndExpiresAtAfter(String token, Instant now);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

}
