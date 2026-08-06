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
    FUND_DISTRIBUTION_NOT_FOUND("error.fund.distribution.not.found", HttpStatus.NOT_FOUND),

    FUND_PERIOD_METRIC_NOT_FOUND("error.fund.period.metric.not.found", HttpStatus.NOT_FOUND),
    FUND_PERIOD_METRIC_ALREADY_EXISTS("error.fund.period.metric.already.exists", HttpStatus.CONFLICT),

    FUND_STOCK_ALLOCATION_NOT_FOUND("error.fund.stock.allocation.not.found", HttpStatus.NOT_FOUND),
    FUND_STOCK_ALLOCATION_ALREADY_EXISTS("error.fund.stock.allocation.already.exists", HttpStatus.CONFLICT),

    FUND_SYNC_FAILED("error.fund.sync.failed", HttpStatus.BAD_GATEWAY),

    INVALID_SCENARIO_TOTAL("fund.scenario.invalid.total", HttpStatus.BAD_REQUEST),
    INVALID_SCENARIO_DEVIATION("fund.scenario.invalid.deviation", HttpStatus.BAD_REQUEST),
    INVALID_SCENARIO_STOCK_FLOOR("fund.scenario.invalid.stock.floor", HttpStatus.BAD_REQUEST),

    INVALID_INPUT("fund.invalid.input", HttpStatus.BAD_REQUEST);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}
