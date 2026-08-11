package com.akademi.finsight.stresstest.llm;

import com.akademi.finsight.stresstest.enums.SimulationType;

public interface LLMCommentGenerator {
    String generateComment(SimulationType scenarioKey);
}
