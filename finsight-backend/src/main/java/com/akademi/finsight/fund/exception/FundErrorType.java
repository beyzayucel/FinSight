package com.akademi.finsight.fund.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FundErrorType implements BaseErrorType {

    FUND_NOT_FOUND("error.fund.not.found", HttpStatus.NOT_FOUND),
    FUND_CODE_ALREADY_EXISTS("error.fund.code.already.exists", HttpStatus.CONFLICT),
    FUND_DISTRIBUTION_NOT_FOUND("error.fund.distribution.not.found", HttpStatus.NOT_FOUND);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}
