package com.akademi.finsight.fund.chat.knowledge;

public record FundChatVocabulary(
        String positive,
        String negative,
        String unchanged,
        String aboveBenchmark,
        String belowBenchmark,
        String unknownValue
) {}
