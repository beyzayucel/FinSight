package com.akademi.finsight.ai.model.repository;

import com.akademi.finsight.ai.model.entity.FundPriceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FundPriceDataRepository extends JpaRepository<FundPriceData, UUID> {

    Optional<FundPriceData> findByFundIdAndDataDate(UUID fundId, LocalDate dataDate);

    Optional<FundPriceData> findFirstByFundCodeOrderByDataDateDesc(String fundCode);

}
