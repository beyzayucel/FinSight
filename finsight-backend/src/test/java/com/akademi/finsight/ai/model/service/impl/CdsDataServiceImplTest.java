package com.akademi.finsight.ai.model.service.impl;

import com.akademi.finsight.ai.model.constant.MarketConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("CdsDataServiceImpl Unit Tests")
class CdsDataServiceImplTest {

    private CdsDataServiceImpl cdsDataService;

    @BeforeEach
    void setUp() {
        cdsDataService = new CdsDataServiceImpl();
    }

    @Nested
    @DisplayName("Data Loading and Initialization (init)")
    class DataInitialization {

        @Test
        @DisplayName("Should successfully load CDS data from classpath CSV on initialization")
        void shouldLoadCdsDataFromCsv() {
            cdsDataService.init();

            // Check if map was populated
            @SuppressWarnings("unchecked")
            NavigableMap<LocalDate, BigDecimal> map = (NavigableMap<LocalDate, BigDecimal>) ReflectionTestUtils.getField(cdsDataService, "cdsSpreadMap");

            assertNotNull(map);
            // The sample file contains data points from 06.08.2026 downwards
            // Let's verify a specific date from the CSV file
            LocalDate testDate = LocalDate.of(2026, 8, 6);
            BigDecimal expectedValue = new BigDecimal("227.65");

            // Query using service method
            BigDecimal actualValue = cdsDataService.getCdsSpreadForDate(testDate);
            assertEquals(expectedValue, actualValue);
        }
    }

    @Nested
    @DisplayName("Query CDS Spread (getCdsSpreadForDate)")
    class QueryCdsSpread {

        private NavigableMap<LocalDate, BigDecimal> mockCdsSpreadMap;

        @BeforeEach
        void setUpMockData() {
            mockCdsSpreadMap = new ConcurrentSkipListMap<>();
            mockCdsSpreadMap.put(LocalDate.of(2026, 8, 1), new BigDecimal("230.50"));
            mockCdsSpreadMap.put(LocalDate.of(2026, 8, 2), new BigDecimal("231.25"));

            // Inject mock map using ReflectionTestUtils
            ReflectionTestUtils.setField(cdsDataService, "cdsSpreadMap", mockCdsSpreadMap);
        }

        @Test
        @DisplayName("Should return the correct CDS spread when the target date exists in the map")
        void shouldReturnSpreadForExistingDate() {
            LocalDate targetDate = LocalDate.of(2026, 8, 1);
            BigDecimal actual = cdsDataService.getCdsSpreadForDate(targetDate);
            assertEquals(new BigDecimal("230.50"), actual);
        }

        @Test
        @DisplayName("Should return default CDS spread when target date does not exist in the map")
        void shouldReturnDefaultSpreadForNonExistingDate() {
            LocalDate targetDate = LocalDate.of(2026, 8, 15);
            BigDecimal actual = cdsDataService.getCdsSpreadForDate(targetDate);
            assertEquals(MarketConstants.DEFAULT_CDS, actual);
        }

        @Test
        @DisplayName("Should return default CDS spread when target date is null")
        void shouldReturnDefaultSpreadForNullDate() {
            BigDecimal actual = cdsDataService.getCdsSpreadForDate(null);
            assertEquals(MarketConstants.DEFAULT_CDS, actual);
        }
    }
}
