package com.akademi.finsight.fund.chat.knowledge;

import java.util.List;

public record FundChatFaqEntry(String id, List<String> keywords, String answer) {}
