package com.akademi.finsight.auth.passwordhistory.repository;

import com.akademi.finsight.auth.passwordhistory.entity.PasswordHistory;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(UUID userId, Limit limit);

    /** Saklanan kayit sayisi sinirlanmazsa tablo kullanici basina sinirsiz buyur. */
    @Modifying
    @Query("""
            DELETE FROM PasswordHistory ph
            WHERE ph.user.id = :userId
              AND ph.id NOT IN (
                  SELECT keep.id FROM PasswordHistory keep
                  WHERE keep.user.id = :userId
                  ORDER BY keep.createdAt DESC
                  LIMIT :keepCount
              )
            """)
    void deleteOlderThanNewest(@Param("userId") UUID userId, @Param("keepCount") int keepCount);
}
