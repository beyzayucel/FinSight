package com.akademi.finsight.fund.chat.dto;

public record FundChatReply(String text, FundChatSource source) {

    public static FundChatReply of(String text, FundChatSource source) {
        return new FundChatReply(text, source);
    }
}
