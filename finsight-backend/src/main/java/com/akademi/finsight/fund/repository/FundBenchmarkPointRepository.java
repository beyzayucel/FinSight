package com.akademi.finsight.fund.repository;

import com.akademi.finsight.fund.entity.FundBenchmarkPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FundBenchmarkPointRepository extends JpaRepository<FundBenchmarkPoint, UUID> {

    Optional<FundBenchmarkPoint> findByFundIdAndDataDate(UUID fundId, LocalDate dataDate);

    @Query("""
            SELECT fbp
            FROM FundBenchmarkPoint fbp
            WHERE fbp.fund.code = :fundCode
              AND fbp.dataDate >= :fromDate
              AND fbp.dataDate <= :toDate
            ORDER BY fbp.dataDate
            """)
    List<FundBenchmarkPoint> findWindowByFundCode(@Param("fundCode") String fundCode,
                                                  @Param("fromDate") LocalDate fromDate,
                                                  @Param("toDate") LocalDate toDate);
}
