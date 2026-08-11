package com.akademi.finsight.fund.chat.constant;

import com.akademi.finsight.common.constants.ApiEndpoints;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FundChatEndpoints {

    public static final String BASE = ApiEndpoints.Funds.BASE;
    public static final String CHAT = "/{fundCode}/chat";
    public static final String CHAT_SESSION = "/{fundCode}/chat/{sessionId}";
}
