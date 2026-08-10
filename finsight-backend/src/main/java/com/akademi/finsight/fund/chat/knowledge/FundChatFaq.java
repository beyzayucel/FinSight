package com.akademi.finsight.fund.chat.knowledge;

import java.util.List;

public record FundChatFaq(String fallback, List<FundChatFaqEntry> entries) {}
