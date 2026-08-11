package com.akademi.finsight.audit.repository;

import com.akademi.finsight.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>,
        JpaSpecificationExecutor<AuditLog> {

    @Modifying
    @Query("UPDATE AuditLog a SET a.archived = true, a.archivedAt = :now " +
            "WHERE a.archived = false AND a.createdAt < :cutoff")
    int archiveOlderThan(@Param("cutoff") Instant cutoff, @Param("now") Instant now);
}
