package com.akademi.finsight.fund.stockprice.repository;

import com.akademi.finsight.fund.stockprice.entity.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, UUID> {

    boolean existsByAssetCode(String assetCode);

    Optional<StockPriceHistory> findByAssetCodeAndDataDate(String assetCode, LocalDate dataDate);

    List<StockPriceHistory> findByAssetCodeAndDataDateBetweenOrderByDataDateAsc(
            String assetCode, LocalDate fromDate, LocalDate toDate);


    @Modifying
    @Query("DELETE FROM StockPriceHistory s WHERE s.assetCode = :assetCode AND s.dataDate < :cutoffDate")
    void deleteByAssetCodeAndDataDateBefore(@Param("assetCode") String assetCode,
                                            @Param("cutoffDate") LocalDate cutoffDate);
}
