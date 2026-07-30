package com.akademi.finsight.security.web;


import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.ErrorDetail;
import com.akademi.finsight.common.web.RequestIdFilter;
import com.akademi.finsight.security.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class FirstLoginInterceptor implements HandlerInterceptor {

    private static final String ERROR_CODE = "PASSWORD_CHANGE_REQUIRED";
    private static final String MESSAGE_KEY = "error.password.change.required";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        String token = jwtService.extractToken(request);
        if (token == null) {
            return true;
        }

        if (jwtService.isFirstLogin(token)) {
            writeErrorResponse(request, response);
            return false;
        }

        return true;
    }

    private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = messageSource.getMessage(MESSAGE_KEY, null, LocaleContextHolder.getLocale());

        var body = ApiStandardResponse.error(
                ErrorDetail.builder(
                        HttpStatus.FORBIDDEN.value(),
                        ERROR_CODE,
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
