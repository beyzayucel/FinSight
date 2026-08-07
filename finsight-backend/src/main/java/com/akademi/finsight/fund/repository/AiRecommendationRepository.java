package com.akademi.finsight.fund.repository;

import com.akademi.finsight.fund.entity.AiRecommendation;
import com.akademi.finsight.fund.entity.RecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, UUID> {

    @Query("""
            SELECT ar
            FROM AiRecommendation ar
            WHERE ar.fund.id = :fundId
              AND ar.user.email = :email
              AND ar.status = :status
            ORDER BY ar.createdAt DESC
            LIMIT 1
  """)
    Optional<AiRecommendation> findLatestByFundAndUserAndStatus(@Param("fundId") UUID fundId, @Param("email") String email, @Param("status") RecommendationStatus status);
}
