package com.akademi.finsight.fund.chat.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FundChatErrorType implements BaseErrorType {

    FUND_CHAT_KNOWLEDGE_UNAVAILABLE("error.fund.chat.knowledge.unavailable", HttpStatus.INTERNAL_SERVER_ERROR),
    FUND_CHAT_PROVIDER_FAILED("error.fund.chat.provider.failed", HttpStatus.BAD_GATEWAY);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}
