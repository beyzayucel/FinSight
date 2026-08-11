package com.akademi.finsight.ai.exception;

import com.akademi.finsight.ai.dto.ProblemDetails;

/**
 * 4xx/5xx cevaplarındaki application/problem+json gövdesini taşır.
 * Dokümana göre client otomatik retry yapmaz; retry politikası çağıran
 * iş akışında açıkça belirlenmelidir.
 */
public class PortfolioApiException extends RuntimeException {

    private final int httpStatus;
    private final ProblemDetails problem;

    public PortfolioApiException(int httpStatus, ProblemDetails problem) {
        super("Portfolio API error %d [%s] requestId=%s: %s".formatted(
                httpStatus,
                problem != null ? problem.code() : "UNKNOWN",
                problem != null ? problem.requestId() : "n/a",
                problem != null ? problem.detail() : "no problem body"));
        this.httpStatus = httpStatus;
        this.problem = problem;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public ProblemDetails problem() {
        return problem;
    }

    /** 503 (model henüz hazır değil) — çağıran taraf burada kısa bekleme sonrası tekrar deneyebilir. */
    public boolean isServiceUnavailable() {
        return httpStatus == 503;
    }

    /** 422 (semantic validation hatası) — retry anlamsızdır, request'i düzeltmek gerekir. */
    public boolean isValidationError() {
        return httpStatus == 422;
    }
}
