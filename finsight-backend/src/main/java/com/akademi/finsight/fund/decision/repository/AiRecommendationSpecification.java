package com.akademi.finsight.fund.decision.repository;

import com.akademi.finsight.fund.decision.entity.AiRecommendation;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AiRecommendationSpecification {

    private static final String USER = "user";
    private static final String ID = "id";
    private static final String STATUS = "status";
    private static final String CREATED_AT = "createdAt";

    /** Karar Raporu yalnızca karara varılmış önerileri gösterir — PENDING henüz bir karar değildir. */
    public static Specification<AiRecommendation> decided() {
        return (root, query, cb) -> cb.notEqual(root.get(STATUS), RecommendationStatus.PENDING);
    }

    public static Specification<AiRecommendation> withUser(UUID userId) {
        if (userId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get(USER).get(ID), userId);
    }

    public static Specification<AiRecommendation> createdSince(Instant since) {
        if (since == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(CREATED_AT), since);
    }
}
