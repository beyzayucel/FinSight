package com.akademi.finsight.fund.entity;

import com.akademi.finsight.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "manual_scenario_weight")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ManualScenarioWeight extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private ManualScenario scenario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCategory category;

    @Column(name = "target_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetWeight;

    @Column(name = "current_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal currentWeight;
}