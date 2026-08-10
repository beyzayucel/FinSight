package com.akademi.finsight.fund.chat.knowledge;

import java.util.List;

public record FundChatIntents(
        List<String> knowledgeFirstMarkers,
        FundChatVocabulary vocabulary,
        List<FundChatIntentEntry> entries
) {}
