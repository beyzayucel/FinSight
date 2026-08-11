package com.akademi.finsight.user.repository;

import com.akademi.finsight.user.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserSpecification {

    private static final String EMAIL = "email";
    private static final String USERNAME = "username";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String ENABLED = "enabled";

    public static Specification<User> hasSearch(String search) {
        if (search == null || search.isBlank()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get(EMAIL)), pattern),
                cb.like(cb.lower(root.get(USERNAME)), pattern),
                cb.like(cb.lower(root.get(FIRST_NAME)), pattern),
                cb.like(cb.lower(root.get(LAST_NAME)), pattern)
        );
    }

    public static Specification<User> hasEnabled(Boolean enabled) {
        if (enabled == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get(ENABLED), enabled);
    }
}
