package com.akademi.finsight.fund.repository;

import com.akademi.finsight.fund.entity.FundStockAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FundStockAllocationRepository extends JpaRepository<FundStockAllocation, UUID> {

    Optional<FundStockAllocation> findByFundIdAndPeriodAndAssetCode(UUID fundId, String period, String assetCode);

    boolean existsByFundIdAndPeriodAndAssetCode(UUID fundId, String period, String assetCode);

    boolean existsByFundIdAndPeriodAndAssetCodeAndIdNot(UUID fundId, String period, String assetCode, UUID id);

    @Query("""
            SELECT fsa
            FROM FundStockAllocation fsa
            WHERE fsa.fund.code = :fundCode
              AND fsa.period = :period
            ORDER BY fsa.weight DESC
            """)
    List<FundStockAllocation> findByFundCodeAndPeriod(@Param("fundCode") String fundCode,
                                                      @Param("period") String period);

    @Query("""
            SELECT MAX(fsa.period)
            FROM FundStockAllocation fsa
            WHERE fsa.fund.code = :fundCode
            """)
    Optional<String> findLatestPeriodByFundCode(@Param("fundCode") String fundCode);
}
