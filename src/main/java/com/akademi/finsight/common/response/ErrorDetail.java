package com.akademi.finsight.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
        int status,
        String code,
        String message,
        String path,
        Instant timestamp,
        List<FieldError> fieldErrors,
        String requestId
) {

    public static Builder builder(int status, String code, String message, String path) {
        return new Builder(status, code, message, path);
    }

    public static final class Builder {
        private final int status;
        private final String code;
        private final String message;
        private final String path;
        private List<FieldError> fieldErrors;
        private String requestId;

        private Builder(int status, String code, String message, String path) {
            this.status = status;
            this.code = code;
            this.message = message;
            this.path = path;
        }

        public Builder fieldErrors(List<FieldError> fieldErrors) {
            this.fieldErrors = fieldErrors;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public ErrorDetail build() {
            return new ErrorDetail(status, code, message, path,
                    Instant.now(), fieldErrors, requestId);
        }
    }
}
