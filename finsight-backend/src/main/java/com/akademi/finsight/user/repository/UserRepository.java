package com.akademi.finsight.user.repository;

import com.akademi.finsight.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);


    @Query("""
            SELECT u
            FROM User u
            WHERE u.email = :identifier
               OR u.username = :identifier
            """)
    Optional<User> findByIdentifier(@Param("identifier") String identifier);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, UUID id);


    long countByEnabled(boolean enabled);

    long countByLastLoginAtAfter(Instant since);
}
