package com.akademi.finsight.fund.repository;


import com.akademi.finsight.fund.entity.ManualScenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ManualScenarioRepository extends JpaRepository<ManualScenario, UUID> {
}
