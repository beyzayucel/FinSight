package com.akademi.finsight.common.controller;

import com.akademi.finsight.common.response.ApiStandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

    protected <T> ResponseEntity<ApiStandardResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiStandardResponse.of(data));
    }

    protected <T> ResponseEntity<ApiStandardResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiStandardResponse.of(data, message));
    }

    protected ResponseEntity<ApiStandardResponse<Void>> ok() {
        return ResponseEntity.ok(ApiStandardResponse.message("OK"));
    }

    protected ResponseEntity<ApiStandardResponse<Void>> ok(String message) {
        return ResponseEntity.ok(ApiStandardResponse.message(message));
    }

    protected <T> ResponseEntity<ApiStandardResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiStandardResponse.of(data));
    }

    protected <T> ResponseEntity<ApiStandardResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiStandardResponse.of(data, message));
    }

    protected ResponseEntity<ApiStandardResponse<Void>> created() {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiStandardResponse.message("Created"));
    }

    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

}
