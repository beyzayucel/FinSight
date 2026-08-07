package com.akademi.finsight.stresstest.llm.impl;

import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.stresstest.llm.LLMCommentGenerator;
import org.springframework.stereotype.Component;

@Component("templateLlmGenerator")
public class TemplateLLMCommentGeneratorImpl implements LLMCommentGenerator {

    @Override
    public String generateComment(SimulationType scenarioKey) {
        if (scenarioKey == SimulationType.EQUITY_SHOCK) {
            return "Hisse senedi piyasalarındaki dik düşüş, özkaynak ağırlıklı portföylerde doğrudan kayba yol açar. " +
                    "Nakit ve sabitleştirilmiş getiri kalemleri yüksek olan portföyler bu süreçte tampon görevi görür.";
        }
        return "Faiz artışı hisse senedi değerlemelerinde baskı yaratırken, ters-repo ve vadeli işlem nakit teminatı kalemlerine kısa vadede olumlu yansır. " +
                "Ters-repo ağırlığı yüksek olan portföyler bu senaryoda nispeten daha az kayıpla çıkar.";
    }
}