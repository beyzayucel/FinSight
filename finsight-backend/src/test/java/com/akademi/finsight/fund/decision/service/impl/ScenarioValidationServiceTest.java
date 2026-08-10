package com.akademi.finsight.fund.decision.service.impl;

import com.akademi.finsight.fund.decision.entity.AssetCategory;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScenarioValidationService Unit Tests")
class ScenarioValidationServiceTest {

    private final ScenarioValidationService validationService = new ScenarioValidationService();

    @Nested
    @DisplayName("Asset Category Weights Validation (validate)")
    class ValidateAssetWeights {

        @Test
        @DisplayName("Should pass when total weight is exactly 100.00 and deviations are within limits")
        void shouldPassWithValidWeights() {
            Map<AssetCategory, BigDecimal> target = new EnumMap<>(AssetCategory.class);
            target.put(AssetCategory.STOCK, new BigDecimal("85.00"));
            target.put(AssetCategory.REPO, new BigDecimal("15.00"));

            Map<AssetCategory, BigDecimal> current = new EnumMap<>(AssetCategory.class);
            current.put(AssetCategory.STOCK, new BigDecimal("80.00"));
            current.put(AssetCategory.REPO, new BigDecimal("20.00"));

            assertDoesNotThrow(() -> validationService.validate(target, current));
        }

        @Test
        @DisplayName("Should throw exception when target weights map is null or empty")
        void shouldThrowExceptionWhenWeightsNullOrEmpty() {
            Map<AssetCategory, BigDecimal> current = new EnumMap<>(AssetCategory.class);
            current.put(AssetCategory.STOCK, new BigDecimal("80.00"));

            FundValidationException exNull = assertThrows(FundValidationException.class, () ->
                    validationService.validate(null, current)
            );
            assertEquals(FundErrorType.INVALID_SCENARIO_TOTAL, exNull.getErrorType());

            FundValidationException exEmpty = assertThrows(FundValidationException.class, () ->
                    validationService.validate(new HashMap<>(), current)
            );
            assertEquals(FundErrorType.INVALID_SCENARIO_TOTAL, exEmpty.getErrorType());
        }

        @Test
        @DisplayName("Should throw exception when total weight is less than 99.99")
        void shouldThrowExceptionWhenTotalWeightTooLow() {
            Map<AssetCategory, BigDecimal> target = new EnumMap<>(AssetCategory.class);
            target.put(AssetCategory.STOCK, new BigDecimal("80.00"));
            target.put(AssetCategory.REPO, new BigDecimal("19.00")); // Total 99.00

            Map<AssetCategory, BigDecimal> current = new EnumMap<>(AssetCategory.class);
            current.put(AssetCategory.STOCK, new BigDecimal("80.00"));
            current.put(AssetCategory.REPO, new BigDecimal("20.00"));

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    validationService.validate(target, current)
            );
            assertEquals(FundErrorType.INVALID_SCENARIO_TOTAL, ex.getErrorType());
        }

        @Test
        @DisplayName("Should throw exception when total weight is greater than 100.01")
        void shouldThrowExceptionWhenTotalWeightTooHigh() {
            Map<AssetCategory, BigDecimal> target = new EnumMap<>(AssetCategory.class);
            target.put(AssetCategory.STOCK, new BigDecimal("85.00"));
            target.put(AssetCategory.REPO, new BigDecimal("16.00")); // Total 101.00

            Map<AssetCategory, BigDecimal> current = new EnumMap<>(AssetCategory.class);
            current.put(AssetCategory.STOCK, new BigDecimal("80.00"));
            current.put(AssetCategory.REPO, new BigDecimal("20.00"));

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    validationService.validate(target, current)
            );
            assertEquals(FundErrorType.INVALID_SCENARIO_TOTAL, ex.getErrorType());
        }

        @Test
        @DisplayName("Should throw exception when a target weight is null")
        void shouldThrowExceptionWhenTargetWeightContainsNullValue() {
            Map<AssetCategory, BigDecimal> target = new EnumMap<>(AssetCategory.class);
            target.put(AssetCategory.STOCK, new BigDecimal("85.00"));
            target.put(AssetCategory.REPO, null);

            Map<AssetCategory, BigDecimal> current = new EnumMap<>(AssetCategory.class);
            current.put(AssetCategory.STOCK, new BigDecimal("80.00"));
            current.put(AssetCategory.REPO, new BigDecimal("20.00"));

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    validationService.validate(target, current)
            );
            assertEquals(FundErrorType.INVALID_INPUT, ex.getErrorType());
        }

        @Test
        @DisplayName("Should throw exception when asset category deviation exceeds 10%")
        void shouldThrowExceptionWhenDeviationExceedsLimit() {
            Map<AssetCategory, BigDecimal> target = new EnumMap<>(AssetCategory.class);
            target.put(AssetCategory.STOCK, new BigDecimal("95.00")); // Deviation: 15.00 (from 80.00)
            target.put(AssetCategory.REPO, new BigDecimal("5.00"));

            Map<AssetCategory, BigDecimal> current = new EnumMap<>(AssetCategory.class);
            current.put(AssetCategory.STOCK, new BigDecimal("80.00"));
            current.put(AssetCategory.REPO, new BigDecimal("20.00"));

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    validationService.validate(target, current)
            );
            assertEquals(FundErrorType.INVALID_SCENARIO_DEVIATION, ex.getErrorType());
            assertEquals(AssetCategory.STOCK, ex.getArgs()[0]);
            assertEquals(new BigDecimal("15.00"), ex.getArgs()[1]);
        }

        @Test
        @DisplayName("Should throw exception when stock weight is less than 80.00%")
        void shouldThrowExceptionWhenStockWeightBelowFloor() {
            Map<AssetCategory, BigDecimal> target = new EnumMap<>(AssetCategory.class);
            target.put(AssetCategory.STOCK, new BigDecimal("75.00")); // Below 80.00
            target.put(AssetCategory.REPO, new BigDecimal("25.00"));

            Map<AssetCategory, BigDecimal> current = new EnumMap<>(AssetCategory.class);
            current.put(AssetCategory.STOCK, new BigDecimal("80.00"));
            current.put(AssetCategory.REPO, new BigDecimal("20.00"));

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    validationService.validate(target, current)
            );
            assertEquals(FundErrorType.INVALID_SCENARIO_STOCK_FLOOR, ex.getErrorType());
        }
    }

    @Nested
    @DisplayName("Individual Stock Weights Validation (validateStockWeights)")
    class ValidateStockWeights {

        @Test
        @DisplayName("Should return immediately when targetStockWeights is null or empty")
        void shouldDoNothingWhenStockWeightsNullOrEmpty() {
            Map<String, BigDecimal> current = new HashMap<>();
            current.put("ASELS", new BigDecimal("100.00"));

            assertDoesNotThrow(() -> validationService.validateStockWeights(null, current));
            assertDoesNotThrow(() -> validationService.validateStockWeights(new HashMap<>(), current));
        }

        @Test
        @DisplayName("Should pass when stock weights sum is near 100.00 and deviations are within 5%")
        void shouldPassWithValidStockWeights() {
            Map<String, BigDecimal> target = new HashMap<>();
            target.put("ASELS", new BigDecimal("60.00"));
            target.put("THYAO", new BigDecimal("40.00"));

            Map<String, BigDecimal> current = new HashMap<>();
            current.put("ASELS", new BigDecimal("58.00"));
            current.put("THYAO", new BigDecimal("42.00"));

            assertDoesNotThrow(() -> validationService.validateStockWeights(target, current));
        }

        @Test
        @DisplayName("Should throw exception when target stock weight contains null value")
        void shouldThrowExceptionWhenStockWeightIsNull() {
            Map<String, BigDecimal> target = new HashMap<>();
            target.put("ASELS", new BigDecimal("60.00"));
            target.put("THYAO", null);

            Map<String, BigDecimal> current = new HashMap<>();
            current.put("ASELS", new BigDecimal("60.00"));
            current.put("THYAO", new BigDecimal("40.00"));

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    validationService.validateStockWeights(target, current)
            );
            assertEquals(FundErrorType.INVALID_INPUT, ex.getErrorType());
        }

        @Test
        @DisplayName("Should throw exception when individual stock deviation exceeds 5%")
        void shouldThrowExceptionWhenStockDeviationExceedsLimit() {
            Map<String, BigDecimal> target = new HashMap<>();
            target.put("ASELS", new BigDecimal("66.00")); // Deviation: 6.00 (from 60.00)
            target.put("THYAO", new BigDecimal("34.00"));

            Map<String, BigDecimal> current = new HashMap<>();
            current.put("ASELS", new BigDecimal("60.00"));
            current.put("THYAO", new BigDecimal("40.00"));

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    validationService.validateStockWeights(target, current)
            );
            assertEquals(FundErrorType.INVALID_SCENARIO_STOCK_DEVIATION, ex.getErrorType());
            assertEquals("ASELS", ex.getArgs()[0]);
            assertEquals(new BigDecimal("6.00"), ex.getArgs()[1]);
        }

        @Test
        @DisplayName("Should throw exception when individual stock weights total deviation from 100.00 exceeds 0.05 tolerance")
        void shouldThrowExceptionWhenStockTotalExceedsTolerance() {
            Map<String, BigDecimal> target = new HashMap<>();
            target.put("ASELS", new BigDecimal("60.00"));
            target.put("THYAO", new BigDecimal("40.10")); // Total 100.10 (Tolerance is 0.05)

            Map<String, BigDecimal> current = new HashMap<>();
            current.put("ASELS", new BigDecimal("60.00"));
            current.put("THYAO", new BigDecimal("40.00"));

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    validationService.validateStockWeights(target, current)
            );
            assertEquals(FundErrorType.INVALID_SCENARIO_STOCK_TOTAL, ex.getErrorType());
            assertEquals(new BigDecimal("100.10"), ex.getArgs()[0]);
            assertEquals(new BigDecimal("100.00"), ex.getArgs()[1]);
        }
    }
}
