package com.akademi.finsight.fund.chat.dto;

import java.util.List;

public record FundChatPrompt(
        String fundCode,
        String systemPrompt,
        String glossary,
        FundChatContext context,
        List<FundChatTurn> history,
        String userMessage
) {}
