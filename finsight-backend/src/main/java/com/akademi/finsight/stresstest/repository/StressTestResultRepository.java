package com.akademi.finsight.stresstest.repository;

import com.akademi.finsight.stresstest.entity.StressTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface StressTestResultRepository extends JpaRepository<StressTestResult, UUID> {

    Optional<StressTestResult> findFirstByUserIdAndFundIdOrderByCreatedAtDesc(UUID userId, UUID fundId);

    Optional<StressTestResult> findFirstByUserIdAndFundIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
            UUID userId,
            UUID fundId,
            Instant createdAt
    );
}
