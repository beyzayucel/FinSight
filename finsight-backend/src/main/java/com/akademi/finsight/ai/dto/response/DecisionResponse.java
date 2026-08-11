package com.akademi.finsight.ai.dto.response;

import com.akademi.finsight.ai.dto.Decision;
import com.akademi.finsight.ai.dto.ProposedTransition;
import com.akademi.finsight.ai.model.controller.WarningResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DecisionResponse(
        @JsonProperty("contract_version") String contractVersion,
        @JsonProperty("state") List<Double> state, // ?
        @JsonProperty("decision") Decision decision,
        @JsonProperty("proposed_transition") ProposedTransition proposedTransition,
        @JsonProperty("warnings") List<WarningResponse> warnings
) {
}
