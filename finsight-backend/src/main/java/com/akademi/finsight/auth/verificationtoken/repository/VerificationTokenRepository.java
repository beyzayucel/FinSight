package com.akademi.finsight.auth.verificationtoken.repository;

import com.akademi.finsight.auth.verificationtoken.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);}

