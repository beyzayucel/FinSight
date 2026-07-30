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

}
