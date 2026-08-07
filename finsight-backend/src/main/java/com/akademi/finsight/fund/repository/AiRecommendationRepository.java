package com.akademi.finsight.fund.repository;

import com.akademi.finsight.fund.entity.AiRecommendation;
import com.akademi.finsight.fund.entity.RecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiRecommendationRepository
        extends JpaRepository<AiRecommendation, UUID>, JpaSpecificationExecutor<AiRecommendation> {

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

    // Karar Geçmişi için — kullanıcının bu fonda karar verdiği (PENDING olmayan) AI önerileri, en yeniden en eskiye.
    List<AiRecommendation> findByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(UUID userId, UUID fundId, RecommendationStatus status);

    // "Metrik/stres testi ekle" akışları için — en güncel karara varılmış (PENDING olmayan) öneri.
    Optional<AiRecommendation> findFirstByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(UUID userId, UUID fundId, RecommendationStatus status);
}
