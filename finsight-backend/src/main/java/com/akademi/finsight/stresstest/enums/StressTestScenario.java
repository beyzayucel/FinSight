package com.akademi.finsight.stresstest.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StressTestScenario {

    // TODO: LLM geldiğinde bu kısım değiştirilecek
    HISSE_STRESS(
            "BIST 100 endeksinde anlık %-10 düşüş",
            "Hisse senedi piyasalarındaki dik düşüş, özkaynak ağırlıklı portföylerde doğrudan kayba yol açar. " +
                    "Nakit ve sabitleştirilmiş getiri kalemleri yüksek olan portföyler bu süreçte tampon görevi görür."
    ),
    INTEREST_STRESS(
            "Gösterge faiz oranında +300 baz puan artış",
            "Faiz artışı hisse senedi değerlemelerinde baskı yaratırken, ters-repo ve vadeli işlem nakit teminatı kalemlerine kısa vadede olumlu yansır. " +
                    "Ters-repo ağırlığı yüksek olan portföyler bu senaryoda nispeten daha az kayıpla çıkar."
    );

    private final String description;
    private final String defaultLlmComment;

    public static StressTestScenario fromKey(String key) {
        for (StressTestScenario scenario : values()) {
            if (scenario.name().equalsIgnoreCase(key)) {
                return scenario;
            }
        }
        return HISSE_STRESS; // Fallback varsayılan senaryo
    }
}
