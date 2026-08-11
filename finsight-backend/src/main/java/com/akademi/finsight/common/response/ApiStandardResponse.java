package com.akademi.finsight.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiStandardResponse<T>(
        boolean success,
        T data,
        String message,
        ErrorDetail error
) {

    public static <T> ApiStandardResponse<T> of(T data) {
        return new ApiStandardResponse<>(true, data, null, null);
    }

    public static <T> ApiStandardResponse<T> of(T data, String message) {
        return new ApiStandardResponse<>(true, data, message, null);
    }

    public static ApiStandardResponse<Void> message(String message) {
        return new ApiStandardResponse<>(true, null, message, null);
    }

    public static ApiStandardResponse<Void> error(ErrorDetail errorDetail) {
        return new ApiStandardResponse<>(false, null, null, errorDetail);
    }
}
