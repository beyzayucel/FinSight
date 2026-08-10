package com.akademi.finsight.fund.chat.knowledge;

public record FundChatContent(
        String systemPrompt,
        String glossary,
        FundChatFaq faq,
        FundChatIntents intents
) {}
