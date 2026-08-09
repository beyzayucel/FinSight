package com.akademi.finsight.auth.ratelimiter.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClientIpResolver {

    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    /** Proxy arkasinda getRemoteAddr proxy'nin adresini verir, o durumda tum kullanicilar tek kovaya duserdi. */
    public String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);

        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
