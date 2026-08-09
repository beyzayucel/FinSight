package com.akademi.finsight.integration.infina.mapper;

import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfo;
import com.akademi.finsight.integration.infina.client.dto.fund.FundPortfolioAllocation;
import com.akademi.finsight.integration.infina.dto.response.benchmark.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundPortfolioAllocationResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundReturnResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.akademi.finsight.integration.infina.support.InfinaFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InfinaMapper")
class InfinaMapperTest {

    private final InfinaMapper mapper = new InfinaMapperImpl();

    @Nested
    @DisplayName("toBenchmarkInfoResponse")
    class ToBenchmarkInfoResponse {

        @Test
        @DisplayName("should rename bmYield to benchmarkYield and pass fundYield through")
        void shouldRenameBmYield() {
            BenchmarkInfoResponse response = mapper.toBenchmarkInfoResponse(benchmarkInfo());

            assertEquals(new BigDecimal("4.82"), response.benchmarkYield());
            assertEquals(new BigDecimal("6.16"), response.fundYield());
        }

        @Test
        @DisplayName("should return null for a null source")
        void shouldReturnNullForNullSource() {
            assertNull(mapper.toBenchmarkInfoResponse(null));
        }
    }

    @Nested
    @DisplayName("toBenchmarkInfoResponseList")
    class ToBenchmarkInfoResponseList {

        @Test
        @DisplayName("should map every element preserving order")
        void shouldMapEveryElement() {
            BenchmarkInfo second = benchmarkInfo("BIST 100", new BigDecimal("1.10"), new BigDecimal("2.20"));

            List<BenchmarkInfoResponse> responses =
                    mapper.toBenchmarkInfoResponseList(List.of(benchmarkInfo(), second));

            assertEquals(2, responses.size());
            assertEquals(new BigDecimal("4.82"), responses.get(0).benchmarkYield());
            assertEquals(new BigDecimal("1.10"), responses.get(1).benchmarkYield());
        }

        @Test
        @DisplayName("should return an empty list for a null source instead of null")
        void shouldReturnEmptyListForNullSource() {
            List<BenchmarkInfoResponse> responses = mapper.toBenchmarkInfoResponseList(null);

            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("toFundPortfolioAllocationResponse")
    class ToFundPortfolioAllocationResponse {

        @Test
        @DisplayName("should rename groupPercentage to ratio and mkType to assetType")
        void shouldRenameFields() {
            FundPortfolioAllocationResponse response =
                    mapper.toFundPortfolioAllocationResponse(allocation());

            assertEquals(new BigDecimal("13.44"), response.ratio());
            assertEquals("HS", response.assetType());
            assertEquals("ASELS", response.assetCode());
            assertEquals(ALLOCATION_PERIOD, response.period());
            assertEquals(DISCLOSURE_ID, response.disclosureId());
        }

        @Test
        @DisplayName("should ignore the other percentage fields that share a similar name")
        void shouldNotConfusePercentageFields() {
            FundPortfolioAllocation source = allocation("ASELS", new BigDecimal("13.44"), DISCLOSURE_ID,
                    new BigDecimal("99.99"), new BigDecimal("88.88"));

            assertEquals(new BigDecimal("13.44"), mapper.toFundPortfolioAllocationResponse(source).ratio());
        }

        @Test
        @DisplayName("should return null for a null source")
        void shouldReturnNullForNullSource() {
            assertNull(mapper.toFundPortfolioAllocationResponse(null));
        }
    }

    @Nested
    @DisplayName("toFundPortfolioAllocationResponseList")
    class ToFundPortfolioAllocationResponseList {

        @Test
        @DisplayName("should map every element preserving order")
        void shouldMapEveryElement() {
            FundPortfolioAllocation second = allocation("BIMAS", new BigDecimal("10.55"), 43L);

            List<FundPortfolioAllocationResponse> responses =
                    mapper.toFundPortfolioAllocationResponseList(List.of(allocation(), second));

            assertEquals(2, responses.size());
            assertEquals("ASELS", responses.get(0).assetCode());
            assertEquals("BIMAS", responses.get(1).assetCode());
        }

        @Test
        @DisplayName("should return an empty list for a null source instead of null")
        void shouldReturnEmptyListForNullSource() {
            List<FundPortfolioAllocationResponse> responses =
                    mapper.toFundPortfolioAllocationResponseList(null);

            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("toFundInfoResponse (FonKunye.v2)")
    class ToFundInfoResponse {

        @Test
        @DisplayName("should flatten fundDetail fields and map the asset distribution")
        void shouldFlattenNestedFields() {
            FundInfoResponse response = mapper.toFundInfoResponse(fundInfoData(
                    List.of("P30D"), List.of(new BigDecimal("-7.57")), null, null));

            assertEquals(FUND_NAME, response.name());
            assertEquals(TOTAL_MV, response.totalMarketPrice());
            assertEquals(INVESTOR_COUNT, response.investorCount());

            assertEquals(LocalDate.of(2026, 7, 31), response.date());
            assertEquals(LocalDate.of(2026, 7, 30), response.fundDate());

            assertEquals(1, response.assetDistribution().size());
            assertEquals("Hisse", response.assetDistribution().getFirst().shortDesc());
            assertEquals(new BigDecimal("94.82"), response.assetDistribution().getFirst().ratio());
        }

        @Test
        @DisplayName("should leave flattened fields null when fundDetail is missing")
        void shouldTolerateMissingFundDetail() {
            FundInfoResponse response = mapper.toFundInfoResponse(fundInfoDataWithoutDetail());

            assertNull(response.name());
            assertNull(response.totalMarketPrice());
            assertNull(response.investorCount());
            assertEquals(LocalDate.of(2026, 7, 31), response.date());
        }

        @Test
        @DisplayName("should return null for a null source")
        void shouldReturnNullForNullSource() {
            assertNull(mapper.toFundInfoResponse(null));
        }
    }

    @Nested
    @DisplayName("toFundReturns")
    class ToFundReturns {

        @Test
        @DisplayName("should zip periods, returns, begin dates and benchmarks index by index")
        void shouldZipParallelLists() {
            List<FundReturnResponse> returns = mapper.toFundReturns(fundInfoData(
                    List.of("P10D", "P30D"),
                    List.of(new BigDecimal("-6.16"), new BigDecimal("-7.57")),
                    List.of(LocalDate.of(2026, 7, 21), LocalDate.of(2026, 7, 1)),
                    List.of(new BigDecimal("-4.82"), new BigDecimal("-6.20"))));

            assertEquals(2, returns.size());
            assertEquals(new FundReturnResponse("P10D", new BigDecimal("-6.16"),
                    LocalDate.of(2026, 7, 21), new BigDecimal("-4.82")), returns.get(0));
            assertEquals(new FundReturnResponse("P30D", new BigDecimal("-7.57"),
                    LocalDate.of(2026, 7, 1), new BigDecimal("-6.20")), returns.get(1));
        }

        @Test
        @DisplayName("should truncate to the shorter of periods and fundReturn")
        void shouldTruncateToShorterList() {
            List<FundReturnResponse> returns = mapper.toFundReturns(fundInfoData(
                    List.of("P10D", "P30D", "P90D"),
                    List.of(new BigDecimal("-6.16")),
                    null, null));

            assertEquals(1, returns.size());
            assertEquals("P10D", returns.getFirst().period());
        }

        @Test
        @DisplayName("should return an empty list when periods is null")
        void shouldReturnEmptyWhenPeriodsNull() {
            assertTrue(mapper.toFundReturns(
                    fundInfoData(null, List.of(new BigDecimal("-6.16")), null, null)).isEmpty());
        }

        @Test
        @DisplayName("should return an empty list when fundReturn is null")
        void shouldReturnEmptyWhenFundReturnNull() {
            assertTrue(mapper.toFundReturns(
                    fundInfoData(List.of("P10D"), null, null, null)).isEmpty());
        }

        @Test
        @DisplayName("should leave beginDate and benchmarkReturn null when those lists are absent")
        void shouldTolerateAbsentOptionalLists() {
            List<FundReturnResponse> returns = mapper.toFundReturns(fundInfoData(
                    List.of("P10D"), List.of(new BigDecimal("-6.16")), null, null));

            assertEquals(1, returns.size());
            assertNull(returns.getFirst().beginDate());
            assertNull(returns.getFirst().benchmarkReturn());
        }

        @Test
        @DisplayName("should leave beginDate and benchmarkReturn null for indexes those lists do not reach")
        void shouldTolerateShorterOptionalLists() {
            List<FundReturnResponse> returns = mapper.toFundReturns(fundInfoData(
                    List.of("P10D", "P30D"),
                    List.of(new BigDecimal("-6.16"), new BigDecimal("-7.57")),
                    List.of(LocalDate.of(2026, 7, 21)),
                    List.of(new BigDecimal("-4.82"))));

            assertEquals(2, returns.size());
            assertEquals(LocalDate.of(2026, 7, 21), returns.get(0).beginDate());
            assertEquals(new BigDecimal("-4.82"), returns.get(0).benchmarkReturn());
            assertNull(returns.get(1).beginDate());
            assertNull(returns.get(1).benchmarkReturn());
        }
    }
}
