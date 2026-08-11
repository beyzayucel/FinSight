package com.akademi.finsight.fund.chat.dto;

import java.time.Instant;

public record FundChatTurn(FundChatRole role, String content, Instant at) {

    public static FundChatTurn user(String content, Instant at) {
        return new FundChatTurn(FundChatRole.USER, content, at);
    }

    public static FundChatTurn assistant(String content, Instant at) {
        return new FundChatTurn(FundChatRole.ASSISTANT, content, at);
    }
}
