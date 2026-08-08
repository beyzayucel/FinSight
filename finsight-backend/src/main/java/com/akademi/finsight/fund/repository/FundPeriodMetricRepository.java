package com.akademi.finsight.fund.repository;

import com.akademi.finsight.fund.entity.FundPeriodMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FundPeriodMetricRepository extends JpaRepository<FundPeriodMetric, UUID> {

    Optional<FundPeriodMetric> findByFundIdAndDataDateAndPeriod(UUID fundId, LocalDate dataDate, String period);

    boolean existsByFundIdAndDataDateAndPeriod(UUID fundId, LocalDate dataDate, String period);

    boolean existsByFundIdAndDataDateAndPeriodAndIdNot(UUID fundId, LocalDate dataDate, String period, UUID id);

    @Query("""
            SELECT fpm
            FROM FundPeriodMetric fpm
            WHERE fpm.fund.code = :fundCode
              AND fpm.dataDate = (
                  SELECT MAX(latest.dataDate)
                  FROM FundPeriodMetric latest
                  WHERE latest.fund.code = :fundCode
              )
            ORDER BY fpm.period
            """)
    List<FundPeriodMetric> findLatestByFundCode(@Param("fundCode") String fundCode);

    @Query("""
            SELECT fpm
            FROM FundPeriodMetric fpm
            WHERE fpm.fund.code = :fundCode
              AND fpm.period = :period
              AND fpm.dataDate = (
                  SELECT MAX(latest.dataDate)
                  FROM FundPeriodMetric latest
                  WHERE latest.fund.code = :fundCode
              )
            """)
    Optional<FundPeriodMetric> findLatestByFundCodeAndPeriod(@Param("fundCode") String fundCode,
                                                             @Param("period") String period);
}
