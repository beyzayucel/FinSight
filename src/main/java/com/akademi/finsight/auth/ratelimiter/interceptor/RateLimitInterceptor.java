package com.akademi.finsight.auth.ratelimiter.interceptor;

import com.akademi.finsight.auth.ratelimiter.filter.CachedBodyHttpServletRequest;
import com.akademi.finsight.auth.ratelimiter.service.LoginRateLimitService;
import com.akademi.finsight.auth.ratelimiter.util.IdentifierHasher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final LoginRateLimitService loginRateLimitService;
    private final ObjectMapper objectMapper;
    private final IdentifierHasher identifierHasher;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String email = extractEmail(request);
        String hashedEmail = identifierHasher.hash(email);
        loginRateLimitService.checkAttemptsOrThrow(hashedEmail);
        return true;
    }

    private String extractEmail(HttpServletRequest request){
        CachedBodyHttpServletRequest cachedRequest =
                (CachedBodyHttpServletRequest) request;
        String body = new String(cachedRequest.getCachedBody(), StandardCharsets.UTF_8);

        JsonNode jsonNode = objectMapper.readTree(body);

        if (jsonNode == null || jsonNode.isNull()) {
            return "";
        }

        return jsonNode.get("email").asString();
    }
}
