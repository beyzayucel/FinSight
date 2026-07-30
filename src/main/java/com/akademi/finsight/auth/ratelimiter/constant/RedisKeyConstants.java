package com.akademi.finsight.auth.ratelimiter.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RedisKeyConstants {
    public static final String LOGIN_ATTEMPTS_KEY = "login:attempts:%s";
    public static final String LOGIN_BLOCKED_LIST = "login:blocked:list:%s";
}
