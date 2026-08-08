package com.akademi.finsight.stresstest.engine;

import com.akademi.finsight.stresstest.enums.ExecutionStrategyType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class StressTestStrategyFactory {

    private final Map<ExecutionStrategyType, StressTestCalculationEngine> engines =
            new EnumMap<>(ExecutionStrategyType.class);

    // Spring, StressTestCalculationEngine olan sınıfları otomatik olarak buraya getirir
    public StressTestStrategyFactory(List<StressTestCalculationEngine> engineList) {
        for (StressTestCalculationEngine engine : engineList) {
            engines.put(engine.getStrategyType(), engine);
        }
    }

    public StressTestCalculationEngine getEngine(ExecutionStrategyType strategyType) {
        return Optional.ofNullable(engines.get(strategyType))
                .orElseThrow(() -> new IllegalArgumentException("Desteklenmeyen strateji tipi: " + strategyType));
    }
}
