package com.akademi.finsight.fund.exception;

import com.akademi.finsight.common.exception.BaseException;

public class AiRecommendationNotFoundException extends BaseException {

    public AiRecommendationNotFoundException() {
        super(FundErrorType.RECOMMENDATION_NOT_FOUND);
    }
}
