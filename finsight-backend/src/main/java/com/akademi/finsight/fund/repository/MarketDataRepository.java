package com.akademi.finsight.fund.repository;

import com.akademi.finsight.fund.entity.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, UUID> {

    Optional<MarketData> findFirstByOrderByDataDateDesc();

    Optional<MarketData> findByDataDate(LocalDate dataDate);
}
