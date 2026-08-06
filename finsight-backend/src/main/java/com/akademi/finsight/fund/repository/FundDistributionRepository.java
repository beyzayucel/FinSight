package com.akademi.finsight.fund.repository;

import com.akademi.finsight.fund.entity.FundDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FundDistributionRepository extends JpaRepository<FundDistribution, UUID> {

    Optional<FundDistribution> findByFundIdAndCategoryAndDate(UUID fundId, String category, LocalDate date);

    @Query("""
            SELECT fd
            FROM FundDistribution fd
            WHERE fd.fund.code = :fundCode
              AND fd.date = (
                  SELECT MAX(latest.date)
                  FROM FundDistribution latest
                  WHERE latest.fund.code = :fundCode
              )
            """)
    List<FundDistribution> findLatestByFundCode(@Param("fundCode") String fundCode);
}
