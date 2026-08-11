package com.akademi.finsight.fund.chat.memory;

import com.akademi.finsight.fund.chat.dto.FundChatTurn;

import java.util.List;

public interface FundChatMemoryStore {

    List<FundChatTurn> load(String userIdentifier, String fundCode, String sessionId);

    void save(String userIdentifier, String fundCode, String sessionId, List<FundChatTurn> turns);

    void clear(String userIdentifier, String fundCode, String sessionId);
}
