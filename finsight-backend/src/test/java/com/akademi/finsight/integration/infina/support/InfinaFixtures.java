package com.akademi.finsight.integration.infina.support;

import com.akademi.finsight.integration.infina.client.dto.base.InfinaResponse;
import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfo;
import com.akademi.finsight.integration.infina.client.dto.fund.FundAssetDistribution;
import com.akademi.finsight.integration.infina.client.dto.fund.FundInfo;
import com.akademi.finsight.integration.infina.client.dto.fund.FundInfoData;
import com.akademi.finsight.integration.infina.client.dto.fund.FundPortfolioAllocation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class InfinaFixtures {

    public static final int SUCCESS_CODE = 200;

    public static final String FUND_CODE = "TIE";
    public static final String BEGIN_PERIOD = "2026-07-01";
    public static final String END_PERIOD = "2026-07-31";
    public static final String CURRENCY = "TRY";
    public static final String DATE = "2026-07-31";
    public static final String PERIODS = "P30D";
    public static final String ALLOCATION_PERIOD = "2026-07";
    public static final long DISCLOSURE_ID = 42L;

    public static final String FUND_NAME = "IS PORTFOY BIST 30 ENDEKSI HISSE SENEDI FONU";
    public static final BigDecimal TOTAL_MV = new BigDecimal("1699484991.50");
    public static final int INVESTOR_COUNT = 1234;

    private InfinaFixtures() {
    }

    // --- InfinaResponse  -------------------------------------------------

    public static <T> InfinaResponse<T> success(T data) {
        return withResultCode(data, SUCCESS_CODE);
    }

    public static <T> InfinaResponse<T> withResultCode(T data, int resultCode) {
        return new InfinaResponse<>(new InfinaResponse.Result<>(
                data, new InfinaResponse.Summary(resultCode, "message", 1)));
    }

    public static <T> InfinaResponse<T> withNullResult() {
        return new InfinaResponse<>(null);
    }

    public static <T> InfinaResponse<T> withNullSummary(T data) {
        return new InfinaResponse<>(new InfinaResponse.Result<>(data, null));
    }

    public static <T> InfinaResponse<T> withNullData() {
        return new InfinaResponse<>(new InfinaResponse.Result<>(
                null, new InfinaResponse.Summary(SUCCESS_CODE, "message", 0)));
    }

    // --- BenchmarkInfo -----------------------------------------------------------

    public static BenchmarkInfo benchmarkInfo() {
        return benchmarkInfo("BIST 30 Endeksi", new BigDecimal("4.82"), new BigDecimal("6.16"));
    }

    public static BenchmarkInfo benchmarkInfo(String definition, BigDecimal bmYield, BigDecimal fundYield) {
        return new BenchmarkInfo(
                LocalDate.of(2026, 1, 1), definition,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), bmYield,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), fundYield);
    }

    // --- FundPortfolioAllocation -------------------------------------------------

    public static FundPortfolioAllocation allocation() {
        return allocation("ASELS", new BigDecimal("13.44"), DISCLOSURE_ID);
    }

    public static FundPortfolioAllocation allocation(String assetCode,
                                                     BigDecimal groupPercentage,
                                                     Long disclosureId) {
        return allocation(assetCode, groupPercentage, disclosureId, null, null);
    }

    public static FundPortfolioAllocation allocation(String assetCode,
                                                     BigDecimal groupPercentage,
                                                     Long disclosureId,
                                                     BigDecimal totalTdPercentage,
                                                     BigDecimal totalPdPercentage) {
        return new FundPortfolioAllocation(
                assetCode, "Aselsan", FUND_CODE, ALLOCATION_PERIOD, "PDF",
                null, null, groupPercentage, totalTdPercentage, totalPdPercentage,
                null, null, null, null, disclosureId, "HS", null);
    }

    // --- FonKunye.v2 (FundInfoData) ----------------------------------------------

    public static FundInfoData fundInfoData() {
        return fundInfoData(List.of(PERIODS), List.of(new BigDecimal("-7.57")), null, null);
    }

    public static FundInfoData fundInfoData(List<String> periods,
                                            List<BigDecimal> fundReturn,
                                            List<LocalDate> fundBeginDates,
                                            List<BigDecimal> fundBenchmark) {
        return new FundInfoData(
                FUND_CODE, LocalDate.of(2026, 7, 31), LocalDate.of(2026, 7, 30), null,
                periods, fundBeginDates, null, null, null, fundDetail(),
                fundReturn, null, null, fundBenchmark, null, null, null, null, null,
                List.of(assetDistribution()));
    }

    public static FundInfoData fundInfoDataWithoutDetail() {
        return new FundInfoData(
                FUND_CODE, LocalDate.of(2026, 7, 31), null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);
    }

    public static FundInfo fundDetail() {
        return new FundInfo(
                FUND_NAME, null, null, null, null,
                null, null, false, null, null,
                null, null, null, null, null,
                TOTAL_MV, INVESTOR_COUNT, null, null, null,
                null, null, null, null, null,
                null, null, null, null);
    }

    public static FundAssetDistribution assetDistribution() {
        return new FundAssetDistribution(
                "HS", new BigDecimal("94.82"), "Hisse Senedi", "Stock", "Hisse", "Stock");
    }
}
