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
        public static final String VERIFY = "/verify";
        public static final String OTP_VERIFY = "/otp/verify";
        public static final String OTP_RESEND = "/otp/resend";
        public static final String FORGOT_PASSWORD = "/forgot-password";
        public static final String RESET_PASSWORD = "/reset-password";
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

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Funds {
        public static final String BASE = API_V1 + "/funds";
        public static final String BY_ID = "/{id}";
        public static final String MANUAL_SCENARIO = "/scenarios/apply";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FundDistributions {
        public static final String BASE = API_V1 + "/fund-distributions";
        public static final String BY_ID = "/{id}";
        public static final String LATEST_BY_FUND = "/funds/{fundCode}/latest";
    }

    private static final String[] PUBLIC_ENDPOINTS = {
            Auth.BASE + Auth.LOGIN,
            Auth.BASE + Auth.REFRESH,
            Auth.BASE + Auth.VERIFY,
            Auth.BASE + Auth.OTP_VERIFY,
            Auth.BASE + Auth.OTP_RESEND,
            Auth.BASE + Auth.FORGOT_PASSWORD,
            Auth.BASE + Auth.RESET_PASSWORD,
            News.BASE,
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    public static String[] getPublicEndpoints() {
        return PUBLIC_ENDPOINTS.clone();
    }

}
