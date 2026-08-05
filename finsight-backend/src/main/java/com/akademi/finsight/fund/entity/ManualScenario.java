package com.akademi.finsight.fund.entity;

import com.akademi.finsight.common.entity.SoftDeletableEntity;
import com.akademi.finsight.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "manual_scenario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ManualScenario extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Column(length = 500)
    private String note;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "category")
    @MapKeyEnumerated(EnumType.STRING)
    @Builder.Default
    private Map<AssetCategory, ManualScenarioWeight> weights = new HashMap<>();

    public void addWeight(ManualScenarioWeight weight) {
        weights.put(weight.getCategory(), weight);
        weight.setScenario(this);
    }

}