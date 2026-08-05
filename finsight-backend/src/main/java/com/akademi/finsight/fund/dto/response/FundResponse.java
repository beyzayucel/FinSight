package com.akademi.finsight.fund.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FundResponse(
        UUID id,
        String code,
        LocalDate date,
        Instant createdAt
) {}
