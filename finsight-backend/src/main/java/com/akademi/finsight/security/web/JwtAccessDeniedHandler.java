package com.akademi.finsight.security.web;


import com.akademi.finsight.common.exception.ErrorType;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.ErrorDetail;
import com.akademi.finsight.common.web.RequestIdFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("Access denied: event=ACCESS_DENIED, path={}", request.getRequestURI());

        ErrorType errorType = ErrorType.ACCESS_DENIED;
        String message = messageSource.getMessage(
                errorType.getMessageKey(), null, LocaleContextHolder.getLocale());

        var body = ApiStandardResponse.error(
                ErrorDetail.builder(
                        HttpStatus.FORBIDDEN.value(),
                        errorType.getCode(),
                        message,
                        request.getRequestURI())
                        .requestId(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY))
                        .build());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), body);
    }
}
