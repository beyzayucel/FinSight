package com.akademi.finsight.auth.ratelimiter.interceptor;

import com.akademi.finsight.auth.ratelimiter.service.PasswordResetRateLimitService;
import com.akademi.finsight.auth.ratelimiter.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Token gonderim ucu icin yalnizca IP bazli sinir: govdede kullaniciyi tanimlayan
 * bir alan yok (yalnizca token), dolayisiyla e-posta bazli sayim mumkun degil.
 */
@Component
@RequiredArgsConstructor
public class PasswordResetSubmitRateLimitInterceptor implements HandlerInterceptor {

    private final PasswordResetRateLimitService passwordResetRateLimitService;
    private final ClientIpResolver clientIpResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        passwordResetRateLimitService.checkAndCountSubmitOrThrow(clientIpResolver.resolve(request));
        return true;
    }
}
