package com.akademi.finsight.stresstest.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StressTestErrorType implements BaseErrorType {
    MODEL_NOT_FOUND("error.stresstest.model.not.found", HttpStatus.NOT_FOUND),
    INVALID_MODEL_KEY("error.stresstest.invalid.model.key", HttpStatus.BAD_REQUEST),
    MODEL_INITIALIZATION_ERROR("error.stresstest.model.init.error", HttpStatus.INTERNAL_SERVER_ERROR),
    MODEL_NOT_AVAILABLE("error.stresstest.model.not.available", HttpStatus.SERVICE_UNAVAILABLE),
    MODEL_INFERENCE_ERROR("error.stresstest.model.inference.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_SIMULATION_TYPE("error.stresstest.invalid.simulation.type", HttpStatus.BAD_REQUEST);

    StressTestErrorType(String s, HttpStatus httpStatus) {
    }

    @Override
    public String getMessageKey() {
        return "";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return null;
    }

    @Override
    public String getCode() {
        return "";
    }
}
