package com.akademi.finsight.auth.ratelimiter.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RedisKeyConstants {
    public static final String LOGIN_ATTEMPTS_KEY = "login:attempts:%s";
    public static final String LOGIN_BLOCKED_LIST = "login:blocked:list:%s";
    public static final String PASSWORD_RESET_EMAIL_REQUESTS_KEY = "password-reset:requests:email:%s";
    public static final String PASSWORD_RESET_EMAIL_COOLDOWN_KEY = "password-reset:cooldown:email:%s";
}
