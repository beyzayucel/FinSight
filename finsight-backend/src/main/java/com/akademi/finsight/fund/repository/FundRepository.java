package com.akademi.finsight.fund.repository;

import com.akademi.finsight.fund.entity.Fund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FundRepository extends JpaRepository<Fund, UUID> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);
}
