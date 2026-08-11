package com.akademi.finsight.auth.ratelimiter.interceptor;

import com.akademi.finsight.auth.ratelimiter.exception.RateLimitErrorType;
import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;
import com.akademi.finsight.auth.ratelimiter.filter.CachedBodyHttpServletRequest;
import com.akademi.finsight.auth.ratelimiter.service.LoginRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final LoginRateLimitService loginRateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String email = extractIdentifier(request);
        loginRateLimitService.checkAttemptsOrThrow(email);
        return true;
    }

    private String extractIdentifier(HttpServletRequest request){

        CachedBodyHttpServletRequest cachedRequest =
                WebUtils.getNativeRequest(request, CachedBodyHttpServletRequest.class);

        if(cachedRequest == null){
            throw new RateLimitException(RateLimitErrorType.INVALID_REQUEST);
        }

        try {
            String body = new String(cachedRequest.getCachedBody(), StandardCharsets.UTF_8);

            JsonNode root = objectMapper.readTree(body);

            JsonNode identifierNode = root.get("identifier");

            if (identifierNode == null || identifierNode.isNull()) {
                throw new RateLimitException(RateLimitErrorType.IDENTIFIER_MISSING);
            }

            return identifierNode.asString();

        }catch (RateLimitException e){
            throw e;
        }catch (Exception e) {
            throw new RateLimitException(RateLimitErrorType.INVALID_REQUEST, e);
        }
    }
}
