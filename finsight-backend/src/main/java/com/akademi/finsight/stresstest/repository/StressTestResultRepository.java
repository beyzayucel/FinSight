package com.akademi.finsight.stresstest.repository;

import com.akademi.finsight.stresstest.entity.StressTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StressTestResultRepository extends JpaRepository<StressTestResult, UUID> {

    Optional<StressTestResult> findFirstByUserIdAndFundIdOrderByCreatedAtDesc(UUID userId, UUID fundId);

    @Query("""
        SELECT r FROM StressTestResult r 
        LEFT JOIN FETCH r.details 
        WHERE r.user.id = :userId 
          AND r.fund.id = :fundId 
          AND r.deleted = false 
          AND r.createdAt >= :lowerBound 
          AND r.createdAt <= :upperBound 
        ORDER BY r.createdAt ASC 
        LIMIT 1
    """)
    Optional<StressTestResult> findFirstByPeriod(
            @Param("userId") UUID userId,
            @Param("fundId") UUID fundId,
            @Param("lowerBound") Instant lowerBound,
            @Param("upperBound") Instant upperBound
    );
}
