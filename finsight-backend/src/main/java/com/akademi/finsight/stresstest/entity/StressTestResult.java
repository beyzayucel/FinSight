package com.akademi.finsight.stresstest.entity;

import com.akademi.finsight.common.entity.SoftDeletableEntity;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stress_test_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE stress_test_results SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
public class StressTestResult extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_stress_test_result_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_stress_test_result_fund"))
    private Fund fund;

    @Enumerated(EnumType.STRING)
    @Column(name = "simulation_type", nullable = false)
    private SimulationType simulationType;

    @OneToMany(mappedBy = "stressTestResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StressTestResultDetail> details = new ArrayList<>();

    public void addDetail(StressTestResultDetail detail) {
        this.details.add(detail);
        detail.setStressTestResult(this);
    }


}
