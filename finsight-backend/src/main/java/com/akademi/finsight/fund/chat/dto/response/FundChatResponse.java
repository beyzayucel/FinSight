package com.akademi.finsight.fund.chat.dto.response;

import com.akademi.finsight.fund.chat.dto.FundChatSource;

import java.time.Instant;

public record FundChatResponse(
        String sessionId,
        String reply,
        FundChatSource source,
        Instant answeredAt
) {}
