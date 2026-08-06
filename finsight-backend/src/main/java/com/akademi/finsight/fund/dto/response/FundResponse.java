package com.akademi.finsight.fund.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FundResponse(
        UUID id,
        String code,
        String name,
        Instant createdAt
) {}
