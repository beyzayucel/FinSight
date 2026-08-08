package com.akademi.finsight.stresstest.config;

import com.akademi.finsight.stresstest.engine.StressTestCalculationEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class StressTestConfig {

    @Bean
    @Primary
    public StressTestCalculationEngine activeStressTestEngine(
            @Qualifier("ruleBasedStressTestEngine") StressTestCalculationEngine ruleBasedEngine) {
        // Şuan kural tabanlı mock engine'i aktifPrimary bean yapıyoruz.
        // Ar-Ge ONNX modelini bitirince burayı onnxEngine ile değiştirmemiz yepyeni bir geçiş için yeterli olacak.
        return ruleBasedEngine;
    }
}
