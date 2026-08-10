package com.akademi.finsight.fund.chat.service;

import com.akademi.finsight.fund.chat.dto.request.FundChatRequest;
import com.akademi.finsight.fund.chat.dto.response.FundChatResponse;

public interface FundChatService {

    FundChatResponse ask(String email, String fundCode, FundChatRequest request);

    void reset(String email, String fundCode, String sessionId);
}
