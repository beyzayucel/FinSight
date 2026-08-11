package com.akademi.finsight.fund.chat.provider;

import com.akademi.finsight.fund.chat.dto.FundChatPrompt;
import com.akademi.finsight.fund.chat.dto.FundChatReply;

public interface FundChatProvider {

    FundChatReply generate(FundChatPrompt prompt);
}
