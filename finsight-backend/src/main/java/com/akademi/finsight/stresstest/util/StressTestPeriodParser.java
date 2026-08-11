package com.akademi.finsight.stresstest.util;

import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StressTestPeriodParser {
    public int parseToDays(String period) {
        if (period == null || period.isBlank()) {
            throw new StressTestException(StressTestErrorType.INVALID_ANALYSIS_PERIOD);
        }
        return switch (period.toUpperCase()) {
            case "1M", "30D" -> 30;
            case "3M", "90D" -> 90;
            case "6M", "180D" -> 180;
            case "1Y", "365D" -> 365;
            default -> parseNumericDays(period);
        };
    }

    private int parseNumericDays(String period) {
        try {
            int days = Integer.parseInt(period.trim());
            if (days < 0) {
                throw new StressTestException(StressTestErrorType.INVALID_ANALYSIS_PERIOD);
            }
            return days;
        } catch (NumberFormatException e) {
            log.warn("Invalid analysis period requested: {}", period);
            throw new StressTestException(StressTestErrorType.INVALID_ANALYSIS_PERIOD);
        }
    }
}