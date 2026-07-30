package com.akademi.finsight.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

/**
 * Extends BaseEntity with soft delete support.
 * Entities that should never be hard-deleted extend this class.
 * Each subclass must declare its own @SQLDelete with the correct table name.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
@SQLRestriction("deleted = 0")
public abstract class SoftDeletableEntity extends BaseEntity {

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    private Instant deletedAt;

}
