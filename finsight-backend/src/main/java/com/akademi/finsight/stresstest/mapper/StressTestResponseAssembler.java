package com.akademi.finsight.stresstest.mapper;

import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.stresstest.llm.LLMCommentGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Fills in the parts of {@link StressTestInferenceResponseDto} that are not stored on the entity.
 *
 * <p>{@code llmComment} has no column yet — it is regenerated on every read. Both the Stress Test
 * screen and the Decision History screen must produce it the same way, so the generation lives here
 * instead of being repeated at each call site.
 *
 * <p>NOTE: once the real LLM replaces the template generator the comment stops being deterministic,
 * and it must be persisted alongside the result instead of regenerated here.
 */
@Component
@RequiredArgsConstructor
public class StressTestResponseAssembler {

    private final StressTestResultMapper stressTestResultMapper;
    private final LLMCommentGenerator llmCommentGenerator;

    public StressTestInferenceResponseDto toResponse(StressTestResult entity) {
        if (entity == null) {
            return null;
        }
        return stressTestResultMapper.toInferenceResponse(entity)
                                     .toBuilder()
                                     .llmComment(llmCommentGenerator.generateComment(entity.getSimulationType()))
                                     .build();
    }

    /**
     * Same enrichment for a DTO that was already mapped elsewhere (e.g. by ManualScenarioMapper).
     */
    public StressTestInferenceResponseDto withLlmComment(StressTestInferenceResponseDto response) {
        if (response == null || response.scenarioKey() == null) {
            return response;
        }
        SimulationType simulationType = SimulationType.valueOf(response.scenarioKey());
        return response.toBuilder()
                       .llmComment(llmCommentGenerator.generateComment(simulationType))
                       .build();
    }
}
