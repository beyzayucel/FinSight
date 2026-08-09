package com.akademi.finsight.auth.ratelimiter.keygenerator;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import static com.akademi.finsight.auth.ratelimiter.constant.RedisKeyConstants.*;

@Component
@NoArgsConstructor
public class RateLimitKeyGenerator {

    public String createAttemptKey(String hashedEmail) {
        return String.format(LOGIN_ATTEMPTS_KEY, hashedEmail);
    }

    public String createBlockKey(String hashEmail){
        return String.format(LOGIN_BLOCKED_LIST, hashEmail);
    }

    public String createPasswordResetEmailKey(String hashedEmail) {
        return String.format(PASSWORD_RESET_EMAIL_REQUESTS_KEY, hashedEmail);
    }

    public String createPasswordResetIpKey(String hashedIp) {
        return String.format(PASSWORD_RESET_IP_REQUESTS_KEY, hashedIp);
    }

    public String createPasswordResetSubmitIpKey(String hashedIp) {
        return String.format(PASSWORD_RESET_SUBMIT_IP_KEY, hashedIp);
    }

}
