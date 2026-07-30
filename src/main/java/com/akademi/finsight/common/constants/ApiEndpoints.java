package com.akademi.finsight.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiEndpoints {

    public static final String API_V1 = "/api/v1";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Auth {
        public static final String BASE = API_V1 + "/auth";
        public static final String LOGIN = "/login";
        public static final String REFRESH = "/refresh";
        public static final String LOGOUT = "/logout";
        public static final String CHANGE_PASSWORD = "/change-password";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Users {
        public static final String BASE = API_V1 + "/users";
        public static final String ME = "/me";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class News {
        public static final String BASE = API_V1 + "/news";
    }

    private static final String[] PUBLIC_ENDPOINTS = {
            Auth.BASE + Auth.LOGIN,
            Auth.BASE + Auth.REFRESH,
            News.BASE,
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    public static String[] getPublicEndpoints() {
        return PUBLIC_ENDPOINTS.clone();
    }

}
