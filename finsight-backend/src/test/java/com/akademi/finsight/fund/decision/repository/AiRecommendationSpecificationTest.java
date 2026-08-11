package com.akademi.finsight.fund.decision.repository;

import com.akademi.finsight.fund.decision.entity.AiRecommendation;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiRecommendationSpecification Tests")
class AiRecommendationSpecificationTest {

    @Mock
    private Root<AiRecommendation> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Predicate predicate;

    @Mock
    private Path<Object> path;

    @Mock
    private Path<Instant> createdAtPath;

    @Test
    @DisplayName("decided should exclude PENDING recommendations")
    void decidedShouldExcludePending() {
        doReturn(path).when(root).get("status");
        when(cb.notEqual(path, RecommendationStatus.PENDING)).thenReturn(predicate);

        Predicate result = AiRecommendationSpecification.decided().toPredicate(root, query, cb);

        assertSame(predicate, result);
    }

    @Test
    @DisplayName("withUser should filter on the user id when a user is given")
    void withUserShouldFilterByUserId() {
        UUID userId = UUID.randomUUID();
        Path<Object> userPath = Mockito.mock(Path.class);
        doReturn(userPath).when(root).get("user");
        doReturn(path).when(userPath).get("id");
        when(cb.equal(path, userId)).thenReturn(predicate);

        Predicate result = AiRecommendationSpecification.withUser(userId).toPredicate(root, query, cb);

        assertSame(predicate, result);
    }

    @Test
    @DisplayName("withUser should match every recommendation when no user is given")
    void withUserShouldBeNoOpWhenNull() {
        when(cb.conjunction()).thenReturn(predicate);

        Predicate result = AiRecommendationSpecification.withUser(null).toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb).conjunction();
    }

    @Test
    @DisplayName("createdSince should filter on createdAt when a lower bound is given")
    void createdSinceShouldFilterByCreatedAt() {
        Instant since = Instant.parse("2026-08-01T00:00:00Z");
        doReturn(createdAtPath).when(root).get("createdAt");
        when(cb.greaterThanOrEqualTo(createdAtPath, since)).thenReturn(predicate);

        Predicate result = AiRecommendationSpecification.createdSince(since).toPredicate(root, query, cb);

        assertSame(predicate, result);
    }

    @Test
    @DisplayName("createdSince should match every recommendation when no lower bound is given")
    void createdSinceShouldBeNoOpWhenNull() {
        when(cb.conjunction()).thenReturn(predicate);

        Predicate result = AiRecommendationSpecification.createdSince(null).toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb).conjunction();
    }
}
