package com.akademi.finsight.fund.dto.response;

import com.akademi.finsight.fund.entity.AssetCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record FundActivePortfolioResponse(
        UUID id,
        String code,
        String name,
        LocalDate date,
        Map<AssetCategory, BigDecimal> weights
) {}
