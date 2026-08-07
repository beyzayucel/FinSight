package com.akademi.finsight.fund.repository;

import com.akademi.finsight.fund.entity.ManualScenario;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ManualScenarioSpecification {

    private static final String USER = "user";
    private static final String ID = "id";
    private static final String CREATED_AT = "createdAt";

    public static Specification<ManualScenario> withUser(UUID userId) {
        if (userId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get(USER).get(ID), userId);
    }

    public static Specification<ManualScenario> createdSince(Instant since) {
        if (since == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(CREATED_AT), since);
    }
}
