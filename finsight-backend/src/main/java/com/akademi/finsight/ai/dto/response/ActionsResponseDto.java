package com.akademi.finsight.ai.dto.response;

import java.util.List;

public record ActionsResponseDto(
        String contractVersion,
        List<ActionDescription> actions
) {
    public record ActionDescription(Integer id, String name, String labelTr, String shortLabelTr) {
    }
}
