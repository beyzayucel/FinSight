package com.akademi.finsight.audit.repository;

import com.akademi.finsight.audit.entity.AuditLog;
import com.akademi.finsight.audit.entity.AuditLogScope;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuditLogSpecification {

    private static final String ARCHIVED = "archived";
    private static final String ACTOR_FULL_NAME = "actorFullName";
    private static final String TARGET_FULL_NAME = "targetFullName";

    public static Specification<AuditLog> withScope(AuditLogScope scope) {
        if (scope == null || scope == AuditLogScope.ALL) {
            return (root, query, cb) -> cb.conjunction();
        }
        boolean archivedValue = (scope == AuditLogScope.ARCHIVED);
        return (root, query, cb) -> cb.equal(root.get(ARCHIVED), archivedValue);
    }

    public static Specification<AuditLog> withSearch(String search) {
        if (search == null || search.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get(ACTOR_FULL_NAME)), pattern),
                cb.like(cb.lower(root.get(TARGET_FULL_NAME)), pattern)
        );
    }
}
