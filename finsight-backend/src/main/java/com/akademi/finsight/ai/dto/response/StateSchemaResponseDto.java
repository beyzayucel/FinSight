package com.akademi.finsight.ai.dto.response;

import java.util.List;


/// GET /api/v1/state/schema cevabını karşılamak için — API'nin 16 state alanının sırasını/ölçeğini döndüğü endpoint, bunu Java'da okumak için bir DTO gerekiyordu.
public record StateSchemaResponseDto(
        String contractVersion,
        Integer stateDimension,
        List<StateFeatureDescription> features
) {
    public record StateFeatureDescription(
            Integer index,
            String field,
            String path,
            String group,
            String unit,
            Double divisor,
            Boolean scaled,
            String labelTr,
            Double expectedMin,
            Double expectedMax
    ) {
    }
}
