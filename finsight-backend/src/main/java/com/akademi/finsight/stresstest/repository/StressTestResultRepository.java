package com.akademi.finsight.stresstest.repository;

import com.akademi.finsight.stresstest.entity.StressTestResult;
import io.lettuce.core.Value;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StressTestResultRepository extends JpaRepository<StressTestResult, UUID> {
    Optional<StressTestResult> findFirstByUserIdAndFundIdOrderByCreatedAtDesc(UUID id, UUID fundId);
}
