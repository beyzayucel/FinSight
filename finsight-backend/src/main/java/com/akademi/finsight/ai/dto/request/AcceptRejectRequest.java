package com.akademi.finsight.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AcceptRejectRequest(
        @Schema(description = "Kullanıcı/Oturum Kimliği", example = "user_123")
        String sessionId
) {}
