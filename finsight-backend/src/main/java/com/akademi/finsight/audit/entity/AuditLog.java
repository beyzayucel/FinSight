package com.akademi.finsight.audit.entity;

import com.akademi.finsight.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "audit_log")
@SQLDelete(sql = "UPDATE audit_log SET archived = 1, archived_at = SYSDATETIMEOFFSET() WHERE id = ?")
public class AuditLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 50)
    private AuditActionType action;

    @Column(name = "actor_user_id", nullable = false, updatable = false)
    private UUID actorUserId;

    @Column(name = "actor_full_name", nullable = false, updatable = false)
    private String actorFullName;

    @Column(name = "target_user_id", nullable = false, updatable = false)
    private UUID targetUserId;

    @Column(name = "target_full_name", nullable = false, updatable = false)
    private String targetFullName;

    @Column(name = "request_id", updatable = false, length = 36)
    private String requestId;

    @Builder.Default
    @Column(nullable = false)
    private boolean archived = false;

    private Instant archivedAt;
}
