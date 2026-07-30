package com.akademi.finsight.news.exception;

import com.akademi.finsight.common.exception.BaseException;

public class NewsFetchException extends BaseException {

    public NewsFetchException(Throwable cause) {
        super(NewsErrorType.NEWS_FETCH_FAILED, cause);
    }
}