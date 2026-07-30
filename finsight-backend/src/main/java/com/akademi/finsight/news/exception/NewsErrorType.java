package com.akademi.finsight.news.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NewsErrorType implements BaseErrorType {

    NEWS_API_EXCEEDED("error.news.api.quota.exceeded", HttpStatus.TOO_MANY_REQUESTS),
    NEWS_FETCH_FAILED("error.news.fetch.failed", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}