package com.akademi.finsight.common.exception;


import com.akademi.finsight.auth.exception.AuthErrorType;
import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.ErrorDetail;
import com.akademi.finsight.common.response.FieldError;
import com.akademi.finsight.common.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiStandardResponse<Void>> handleBaseException(
            BaseException ex, HttpServletRequest request) {

        String message = resolveMessage(ex.getErrorType());
        return buildResponse(ex.getErrorType(), message, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiStandardResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.debug("Validation failed: path={}, errors={}", request.getRequestURI(), ex.getErrorCount());

        var fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getDefaultMessage() != null
                                ? error.getDefaultMessage() : "Invalid value"
                ))
                .toList();

        ErrorType errorType = ErrorType.VALIDATION_ERROR;
        String message = resolveMessage(errorType);

        return ResponseEntity.status(errorType.getHttpStatus())
                .body(ApiStandardResponse.error(
                        ErrorDetail.builder(
                                        errorType.getHttpStatus().value(),
                                        errorType.getCode(),
                                        message,
                                        request.getRequestURI())
                                .fieldErrors(fieldErrors)
                                .requestId(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY))
                                .build()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiStandardResponse<Void>> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.debug("Malformed request body: path={}", request.getRequestURI());

        return buildResponse(ErrorType.MALFORMED_JSON, resolveMessage(ErrorType.MALFORMED_JSON), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiStandardResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.debug("Method not supported: method={}, path={}", ex.getMethod(), request.getRequestURI());

        return buildResponse(ErrorType.METHOD_NOT_ALLOWED,
                resolveMessage(ErrorType.METHOD_NOT_ALLOWED, ex.getMethod()), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiStandardResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.debug("Missing parameter: name={}, path={}", ex.getParameterName(), request.getRequestURI());

        return buildResponse(ErrorType.MISSING_PARAMETER,
                resolveMessage(ErrorType.MISSING_PARAMETER, ex.getParameterName()), request);
    }



    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiStandardResponse<Void>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        log.warn("Authentication failed: event=LOGIN_FAILED, reason=INVALID_CREDENTIALS");

        return buildResponse(AuthErrorType.INVALID_CREDENTIALS,
                resolveMessage(AuthErrorType.INVALID_CREDENTIALS), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiStandardResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation: event=DATA_CONFLICT, path={}",
                request.getRequestURI(), ex);

        return buildResponse(ErrorType.DATA_CONFLICT,
                resolveMessage(ErrorType.DATA_CONFLICT), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiStandardResponse<Void>> handleUnexpectedException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error: event=INTERNAL_ERROR, path={}",
                request.getRequestURI(), ex);

        return buildResponse(ErrorType.INTERNAL_ERROR, resolveMessage(ErrorType.INTERNAL_ERROR), request);
    }

    private String resolveMessage(BaseErrorType errorType, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(errorType.getMessageKey(), args, locale);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiStandardResponse<Void>> handleRateLimitExceeded(
            RateLimitException ex,
            HttpServletRequest request) {

        log.warn("Rate limit exceeded: path={}, remainingTime={}s",
                request.getRequestURI(),
                ex.getRemainingTime());

        BaseErrorType errorType = ex.getErrorType();
        String message = resolveMessage(errorType);

        return ResponseEntity.status(errorType.getHttpStatus())
                .body(ApiStandardResponse.error(
                        ErrorDetail.builder(
                                        errorType.getHttpStatus().value(),
                                        errorType.getCode(),
                                        message,
                                        request.getRequestURI())
                                .requestId(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY))
                                .remainingTime(ex.getRemainingTime())
                                .build()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiStandardResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Invalid argument provided: path={}, message={}", request.getRequestURI(), ex.getMessage());

        ErrorType errorType = ErrorType.INVALID_INPUT;
        String message = ex.getMessage() != null ? ex.getMessage() : resolveMessage(errorType);

        return buildResponse(errorType, message, request);
    }

    private ResponseEntity<ApiStandardResponse<Void>> buildResponse(
            BaseErrorType errorType, String message, HttpServletRequest request) {

        return ResponseEntity.status(errorType.getHttpStatus())
                .body(ApiStandardResponse.error(
                        ErrorDetail.builder(
                                        errorType.getHttpStatus().value(),
                                        errorType.getCode(),
                                        message,
                                        request.getRequestURI())
                                .requestId(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY))
                                .build()));
    }
}
