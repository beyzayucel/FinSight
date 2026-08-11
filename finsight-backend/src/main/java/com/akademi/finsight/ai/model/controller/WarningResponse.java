package com.akademi.finsight.ai.model.controller;

public record WarningResponse(
        String code,
        String pointer,
        String detail
) {
}
