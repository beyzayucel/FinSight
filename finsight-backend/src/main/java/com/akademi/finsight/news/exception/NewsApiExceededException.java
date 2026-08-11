package com.akademi.finsight.news.exception;

import com.akademi.finsight.common.exception.BaseException;

public class NewsApiExceededException extends BaseException {

    public NewsApiExceededException(Throwable cause) {
        super(NewsErrorType.NEWS_API_EXCEEDED, cause);
    }
}