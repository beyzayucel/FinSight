package com.akademi.finsight.integration.infina.service.impl;

import com.akademi.finsight.integration.infina.client.InfinaServicesClient;
import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfo;
import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfoData;
import com.akademi.finsight.integration.infina.client.dto.fund.FundInfoData;
import com.akademi.finsight.integration.infina.client.dto.fund.FundPortfolioAllocation;
import com.akademi.finsight.integration.infina.client.dto.fund.FundPortfolioAllocationData;
import com.akademi.finsight.integration.infina.dto.response.benchmark.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundPortfolioAllocationResponse;
import com.akademi.finsight.integration.infina.exception.InfinaErrorType;
import com.akademi.finsight.integration.infina.exception.InfinaIntegrationException;
import com.akademi.finsight.integration.infina.mapper.InfinaMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static com.akademi.finsight.integration.infina.support.InfinaFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InfinaServiceImpl")
class InfinaServiceImplTest {

    @Mock
    private InfinaServicesClient infinaServicesClient;

    @Mock
    private InfinaMapper infinaMapper;

    @InjectMocks
    private InfinaServiceImpl infinaService;

    @Nested
    @DisplayName("getBenchmarkInfo")
    class GetBenchmarkInfo {

        @Test
        @DisplayName("should return mapped list and forward every query parameter in order")
        void shouldReturnMappedList() {
            BenchmarkInfo raw = benchmarkInfo();
            List<BenchmarkInfoResponse> expected = List.of(
                    new BenchmarkInfoResponse(new BigDecimal("4.82"), new BigDecimal("6.16")));

            when(infinaServicesClient.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY))
                    .thenReturn(success(new BenchmarkInfoData(List.of(raw))));
            when(infinaMapper.toBenchmarkInfoResponseList(List.of(raw))).thenReturn(expected);

            List<BenchmarkInfoResponse> actual =
                    infinaService.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY);

            assertSame(expected, actual);
            verify(infinaServicesClient).getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY);
        }

        @Test
        @DisplayName("should forward null currency untouched since it is optional upstream")
        void shouldForwardNullCurrency() {
            when(infinaServicesClient.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, null))
                    .thenReturn(success(new BenchmarkInfoData(List.of())));

            infinaService.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, null);

            verify(infinaServicesClient).getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, null);
        }

        @Test
        @DisplayName("should hand a null BenchmarkInfo list to the mapper without guarding it")
        void shouldNotGuardNullInnerList() {
            when(infinaServicesClient.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY))
                    .thenReturn(success(new BenchmarkInfoData(null)));

            infinaService.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY);

            verify(infinaMapper).toBenchmarkInfoResponseList(null);
        }

        @Test
        @DisplayName("should throw INFINA_ERROR_RESPONSE when Infina reports a non-success result code")
        void shouldThrowOnNonSuccessCode() {
            when(infinaServicesClient.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY))
                    .thenReturn(withResultCode(new BenchmarkInfoData(List.of()), 500));

            InfinaIntegrationException exception = assertThrows(InfinaIntegrationException.class,
                    () -> infinaService.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY));

            assertEquals(InfinaErrorType.INFINA_ERROR_RESPONSE, exception.getErrorType());
            verifyNoInteractions(infinaMapper);
        }
    }

    @Nested
    @DisplayName("getFundInfo (FonKunye.v2)")
    class GetFundInfo {

        @Test
        @DisplayName("should map the whole payload and forward every query parameter in order")
        void shouldReturnMappedResponse() {
            FundInfoData raw = fundInfoData();
            FundInfoResponse expected = new FundInfoResponse(
                    FUND_NAME, LocalDate.of(2026, Month.JULY, 31), LocalDate.of(2026, Month.JULY, 30),
                    List.of(), TOTAL_MV, List.of(), INVESTOR_COUNT);

            when(infinaServicesClient.getFundInfo(FUND_CODE, DATE, PERIODS)).thenReturn(success(raw));
            when(infinaMapper.toFundInfoResponse(raw)).thenReturn(expected);

            FundInfoResponse actual = infinaService.getFundInfo(FUND_CODE, DATE, PERIODS);

            assertSame(expected, actual);
            verify(infinaServicesClient).getFundInfo(FUND_CODE, DATE, PERIODS);
        }

        @Test
        @DisplayName("should forward null date and periods untouched since both are optional upstream")
        void shouldForwardNullOptionalParams() {
            when(infinaServicesClient.getFundInfo(FUND_CODE, null, null)).thenReturn(success(fundInfoData()));

            infinaService.getFundInfo(FUND_CODE, null, null);

            verify(infinaServicesClient).getFundInfo(FUND_CODE, null, null);
        }

        @Test
        @DisplayName("should throw INFINA_ERROR_RESPONSE when Infina reports a non-success result code")
        void shouldThrowOnNonSuccessCode() {
            when(infinaServicesClient.getFundInfo(FUND_CODE, DATE, PERIODS))
                    .thenReturn(withResultCode(fundInfoData(), 401));

            InfinaIntegrationException exception = assertThrows(InfinaIntegrationException.class,
                    () -> infinaService.getFundInfo(FUND_CODE, DATE, PERIODS));

            assertEquals(InfinaErrorType.INFINA_ERROR_RESPONSE, exception.getErrorType());
            verifyNoInteractions(infinaMapper);
        }
    }

    @Nested
    @DisplayName("getFundPortfolioAllocation")
    class GetFundPortfolioAllocation {

        @Test
        @DisplayName("should return mapped list and forward every query parameter in order")
        void shouldReturnMappedList() {
            FundPortfolioAllocation raw = allocation();
            List<FundPortfolioAllocationResponse> expected = List.of(
                    new FundPortfolioAllocationResponse(
                            "ASELS", new BigDecimal("13.44"), ALLOCATION_PERIOD, "HS", DISCLOSURE_ID));

            when(infinaServicesClient.getFundPortfolioAllocation(FUND_CODE, ALLOCATION_PERIOD, DISCLOSURE_ID))
                    .thenReturn(success(new FundPortfolioAllocationData(List.of(raw))));
            when(infinaMapper.toFundPortfolioAllocationResponseList(List.of(raw))).thenReturn(expected);

            List<FundPortfolioAllocationResponse> actual =
                    infinaService.getFundPortfolioAllocation(FUND_CODE, ALLOCATION_PERIOD, DISCLOSURE_ID);

            assertSame(expected, actual);
            verify(infinaServicesClient).getFundPortfolioAllocation(FUND_CODE, ALLOCATION_PERIOD, DISCLOSURE_ID);
        }

        @Test
        @DisplayName("should forward null period and disclosureId untouched since both are optional upstream")
        void shouldForwardNullOptionalParams() {
            when(infinaServicesClient.getFundPortfolioAllocation(FUND_CODE, null, null))
                    .thenReturn(success(new FundPortfolioAllocationData(List.of())));

            infinaService.getFundPortfolioAllocation(FUND_CODE, null, null);

            verify(infinaServicesClient).getFundPortfolioAllocation(FUND_CODE, null, null);
        }

        @Test
        @DisplayName("should throw INFINA_ERROR_RESPONSE when Infina reports a non-success result code")
        void shouldThrowOnNonSuccessCode() {
            when(infinaServicesClient.getFundPortfolioAllocation(FUND_CODE, ALLOCATION_PERIOD, DISCLOSURE_ID))
                    .thenReturn(withResultCode(new FundPortfolioAllocationData(List.of()), 503));

            InfinaIntegrationException exception = assertThrows(InfinaIntegrationException.class,
                    () -> infinaService.getFundPortfolioAllocation(FUND_CODE, ALLOCATION_PERIOD, DISCLOSURE_ID));

            assertEquals(InfinaErrorType.INFINA_ERROR_RESPONSE, exception.getErrorType());
            verifyNoInteractions(infinaMapper);
        }
    }

    @Nested
    @DisplayName("callInfina error handling")
    class CallInfinaErrorHandling {

        @Test
        @DisplayName("should throw INFINA_ERROR_RESPONSE when the response itself is null")
        void shouldThrowOnNullResponse() {
            when(infinaServicesClient.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY))
                    .thenReturn(null);

            assertEquals(InfinaErrorType.INFINA_ERROR_RESPONSE, callAndCatch().getErrorType());
            verifyNoInteractions(infinaMapper);
        }

        @Test
        @DisplayName("should throw INFINA_ERROR_RESPONSE when result is null")
        void shouldThrowOnNullResult() {
            when(infinaServicesClient.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY))
                    .thenReturn(withNullResult());

            assertEquals(InfinaErrorType.INFINA_ERROR_RESPONSE, callAndCatch().getErrorType());
            verifyNoInteractions(infinaMapper);
        }

        @Test
        @DisplayName("should throw INFINA_ERROR_RESPONSE when summary is null")
        void shouldThrowOnNullSummary() {
            when(infinaServicesClient.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY))
                    .thenReturn(withNullSummary(new BenchmarkInfoData(List.of())));

            assertEquals(InfinaErrorType.INFINA_ERROR_RESPONSE, callAndCatch().getErrorType());
            verifyNoInteractions(infinaMapper);
        }

        @Test
        @DisplayName("should throw INFINA_ERROR_RESPONSE when data is null")
        void shouldThrowOnNullData() {
            when(infinaServicesClient.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY))
                    .thenReturn(withNullData());

            assertEquals(InfinaErrorType.INFINA_ERROR_RESPONSE, callAndCatch().getErrorType());
            verifyNoInteractions(infinaMapper);
        }

        @Test
        @DisplayName("should translate an HTTP error status into INFINA_ERROR_RESPONSE and keep the cause")
        void shouldTranslateHttpErrorStatus() {
            RestClientResponseException cause =
                    new RestClientResponseException("Infina 502", 502, "Bad Gateway", null, null, null);
            when(infinaServicesClient.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY))
                    .thenThrow(cause);

            InfinaIntegrationException exception = callAndCatch();

            assertEquals(InfinaErrorType.INFINA_ERROR_RESPONSE, exception.getErrorType());
            assertSame(cause, exception.getCause());
            verifyNoInteractions(infinaMapper);
        }

        @Test
        @DisplayName("should translate a transport failure into INFINA_UNAVAILABLE and keep the cause")
        void shouldTranslateTransportFailure() {
            RestClientException cause = new RestClientException("connection refused");
            when(infinaServicesClient.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY))
                    .thenThrow(cause);

            InfinaIntegrationException exception = callAndCatch();

            assertEquals(InfinaErrorType.INFINA_UNAVAILABLE, exception.getErrorType());
            assertSame(cause, exception.getCause());
            verifyNoInteractions(infinaMapper);
        }

        private InfinaIntegrationException callAndCatch() {
            return assertThrows(InfinaIntegrationException.class,
                    () -> infinaService.getBenchmarkInfo(FUND_CODE, BEGIN_PERIOD, END_PERIOD, CURRENCY));
        }
    }
}
