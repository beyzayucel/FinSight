package com.akademi.finsight.auth.ratelimiter.interceptor;

import com.akademi.finsight.auth.ratelimiter.exception.RateLimitErrorType;
import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;
import com.akademi.finsight.auth.ratelimiter.filter.CachedBodyHttpServletRequest;
import com.akademi.finsight.auth.ratelimiter.service.PasswordResetRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

/** Login interceptor'i "identifier" alanini okur; sifre sifirlama govdesinde alan adi "email". */
@Component
@RequiredArgsConstructor
public class PasswordResetRateLimitInterceptor implements HandlerInterceptor {

    private final PasswordResetRateLimitService passwordResetRateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        passwordResetRateLimitService.checkAndCountOrThrow(extractEmail(request));
        return true;
    }

    private String extractEmail(HttpServletRequest request) {
        CachedBodyHttpServletRequest cachedRequest =
                WebUtils.getNativeRequest(request, CachedBodyHttpServletRequest.class);

        if (cachedRequest == null) {
            throw new RateLimitException(RateLimitErrorType.REQUEST_NOT_WRAPPED);
        }

        try {
            String body = new String(cachedRequest.getCachedBody(), StandardCharsets.UTF_8);
            JsonNode emailNode = objectMapper.readTree(body).get("email");

            if (emailNode == null || emailNode.isNull()) {
                throw new RateLimitException(RateLimitErrorType.IDENTIFIER_MISSING);
            }

            return emailNode.asString();
        } catch (RateLimitException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RateLimitException(RateLimitErrorType.INVALID_REQUEST, exception);
        }
    }
}
