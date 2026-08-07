package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.dto.response.FundSyncResponse;
import com.akademi.finsight.fund.dto.sync.FundSyncSnapshot;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.entity.FundBenchmarkPoint;
import com.akademi.finsight.fund.entity.FundDistribution;
import com.akademi.finsight.fund.entity.FundPeriodMetric;
import com.akademi.finsight.fund.entity.FundStockAllocation;
import com.akademi.finsight.fund.repository.FundBenchmarkPointRepository;
import com.akademi.finsight.fund.repository.FundDistributionRepository;
import com.akademi.finsight.fund.repository.FundPeriodMetricRepository;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.repository.FundStockAllocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FundSyncPersister {

    private final FundRepository fundRepository;
    private final FundPeriodMetricRepository fundPeriodMetricRepository;
    private final FundBenchmarkPointRepository fundBenchmarkPointRepository;
    private final FundDistributionRepository fundDistributionRepository;
    private final FundStockAllocationRepository fundStockAllocationRepository;

    public FundSyncResponse persist(FundSyncSnapshot snapshot) {
        Fund fund = upsertFund(snapshot);
        upsertPeriodMetrics(fund, snapshot);
        upsertBenchmarkPoints(fund, snapshot);
        upsertDistributions(fund, snapshot);
        upsertStockAllocations(fund, snapshot);

        log.info("Fund data synced: fundId={}, fundCode={}, dataDate={}, periodMetrics={}, benchmarkPoints={}, "
                        + "distributions={}, allocationPeriod={}, stockAllocations={}",
                fund.getId(), fund.getCode(), snapshot.dataDate(), snapshot.periodMetrics().size(),
                snapshot.benchmarkPoints().size(), snapshot.distributions().size(), snapshot.allocationPeriod(),
                snapshot.stockAllocations().size());

        return new FundSyncResponse(
                fund.getId(),
                fund.getCode(),
                snapshot.dataDate(),
                snapshot.periodMetrics().size(),
                snapshot.benchmarkPoints().size(),
                snapshot.distributions().size(),
                snapshot.allocationPeriod(),
                snapshot.stockAllocations().size());
    }

    private Fund upsertFund(FundSyncSnapshot snapshot) {
        Fund fund = fundRepository.findByCode(snapshot.fundCode())
                .orElseGet(() -> Fund.builder()
                        .code(snapshot.fundCode())
                        .build());

        fund.setName(snapshot.fundName());

        return fundRepository.save(fund);
    }

    private void upsertPeriodMetrics(Fund fund, FundSyncSnapshot snapshot) {
        Instant fetchedAt = Instant.now();

        for (FundSyncSnapshot.PeriodMetric metric : snapshot.periodMetrics()) {
            FundPeriodMetric entity = fundPeriodMetricRepository
                    .findByFundIdAndDataDateAndPeriod(fund.getId(), snapshot.dataDate(), metric.period())
                    .orElseGet(() -> FundPeriodMetric.builder()
                            .fund(fund)
                            .dataDate(snapshot.dataDate())
                            .period(metric.period())
                            .build());

            entity.setTotalValue(snapshot.totalValue());
            entity.setPreviousTotalValue(metric.previousTotalValue());
            entity.setPreviousDate(metric.previousDate());
            entity.setDailyReturn(snapshot.dailyReturn());
            entity.setCumulativeReturn(metric.cumulativeReturn());
            entity.setBenchmarkReturn(metric.benchmarkReturn());
            entity.setFetchedAt(fetchedAt);

            fundPeriodMetricRepository.save(entity);
        }
    }

    private void upsertBenchmarkPoints(Fund fund, FundSyncSnapshot snapshot) {
        Instant fetchedAt = Instant.now();

        for (FundSyncSnapshot.BenchmarkPoint point : snapshot.benchmarkPoints()) {
            FundBenchmarkPoint entity = fundBenchmarkPointRepository
                    .findByFundIdAndDataDate(fund.getId(), point.date())
                    .orElseGet(() -> FundBenchmarkPoint.builder()
                            .fund(fund)
                            .dataDate(point.date())
                            .build());

            entity.setFundReturn(point.fundReturn());
            entity.setBenchmarkReturn(point.benchmarkReturn());
            entity.setFetchedAt(fetchedAt);

            fundBenchmarkPointRepository.save(entity);
        }
    }

    private void upsertDistributions(Fund fund, FundSyncSnapshot snapshot) {
        for (FundSyncSnapshot.Distribution distribution : snapshot.distributions()) {
            FundDistribution entity = fundDistributionRepository
                    .findByFundIdAndCategoryAndDate(fund.getId(), distribution.category(), snapshot.dataDate())
                    .orElseGet(() -> FundDistribution.builder()
                            .fund(fund)
                            .category(distribution.category())
                            .date(snapshot.dataDate())
                            .build());

            entity.setWeight(distribution.weight());

            fundDistributionRepository.save(entity);
        }
    }

    private void upsertStockAllocations(Fund fund, FundSyncSnapshot snapshot) {
        String period = snapshot.allocationPeriod();
        if (period == null) {
            return;
        }

        for (FundSyncSnapshot.StockAllocation allocation : snapshot.stockAllocations()) {
            FundStockAllocation entity = fundStockAllocationRepository
                    .findByFundIdAndPeriodAndAssetCode(fund.getId(), period, allocation.assetCode())
                    .orElseGet(() -> FundStockAllocation.builder()
                            .fund(fund)
                            .period(period)
                            .assetCode(allocation.assetCode())
                            .build());

            entity.setWeight(allocation.weight());

            fundStockAllocationRepository.save(entity);
        }
    }
}
