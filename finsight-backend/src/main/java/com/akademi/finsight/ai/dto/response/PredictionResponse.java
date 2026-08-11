package com.akademi.finsight.ai.dto.response;

import java.util.List;

public record PredictionResponse(
        Integer action,
        String actionName,
        List<Double> qValues
) {
}
