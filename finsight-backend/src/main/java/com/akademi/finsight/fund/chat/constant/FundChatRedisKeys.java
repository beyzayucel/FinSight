package com.akademi.finsight.fund.chat.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FundChatRedisKeys {

    public static final String HISTORY = "fund-chat:history:%s:%s:%s";
}
