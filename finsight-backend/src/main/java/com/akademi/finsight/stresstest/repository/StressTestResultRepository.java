package com.akademi.finsight.stresstest.repository;

import com.akademi.finsight.stresstest.entity.StressTestResult;
import io.lettuce.core.Value;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface StressTestResultRepository extends JpaRepository<StressTestResult, UUID> {
    Optional<StressTestResult> findFirstByUserIdAndFundIdOrderByCreatedAtDesc(String id, String fundId);
    Optional<StressTestResult> findFirstByUserIdAndFundIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
            UUID userId,
            String fundId,
            LocalDateTime targetDateTime
    );

}
