package com.akademi.finsight.fund.chat.dto;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.fund.chat.knowledge.FundChatContent;

import java.util.List;

public record FundChatPrompt(
        String fundCode,
        SupportedLanguage language,
        FundChatContent content,
        FundChatContext context,
        List<FundChatTurn> history,
        String userMessage
) {}
