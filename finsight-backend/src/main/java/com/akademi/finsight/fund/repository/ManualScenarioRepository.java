package com.akademi.finsight.fund.repository;


import com.akademi.finsight.fund.entity.ManualScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManualScenarioRepository
        extends JpaRepository<ManualScenario, UUID>, JpaSpecificationExecutor<ManualScenario> {

    List<ManualScenario> findByUserIdAndFundIdOrderByCreatedAtDesc(UUID userId, UUID fundId);

    Optional<ManualScenario> findFirstByUserIdAndFundIdOrderByCreatedAtDesc(UUID userId, UUID fundId);
}
