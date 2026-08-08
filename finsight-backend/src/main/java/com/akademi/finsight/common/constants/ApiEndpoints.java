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
        public static final String BY_ID = "/{id}";
        public static final String STATUS = "/{id}/status";
        public static final String STATS = "/stats";
        public static final String RESEND_VERIFICATION = "/{id}/resend-verification";
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
        public static final String MANUAL_SCENARIO_HISTORY = "/scenarios/history";
        public static final String DECISION_HISTORY = "/decisions/history";
        public static final String DECISION_METRICS = "/decisions/metrics";
        public static final String DECISION_STRESS_TEST = "/decisions/stress-test";
        public static final String SYNC = "/sync";
        public static final String DASHBOARD = "/{fundCode}/dashboard";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FundDistributions {
        public static final String BASE = API_V1 + "/fund-distributions";
        public static final String BY_ID = "/{id}";
        public static final String LATEST_BY_FUND = "/funds/{fundCode}/latest";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FundPeriodMetrics {
        public static final String BASE = API_V1 + "/fund-period-metrics";
        public static final String BY_ID = "/{id}";
        public static final String LATEST_BY_FUND = "/funds/{fundCode}/latest";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FundStockAllocations {
        public static final String BASE = API_V1 + "/fund-stock-allocations";
        public static final String BY_ID = "/{id}";
        public static final String BY_FUND_AND_PERIOD = "/funds/{fundCode}/periods/{period}";
        public static final String BREAKDOWN_BY_FUND = "/funds/{fundCode}/breakdown";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class StressTest {
        public static final String BASE = API_V1 + "/stress-tests";
        public static final String RUN = "/run";
        public static final String LATEST = "/latest";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class AuditLogs {
        public static final String BASE = API_V1 + "/audit-logs";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class AdminDecisions {
        public static final String BASE = API_V1 + "/admin/decisions";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class PerformanceComparison {
        public static final String BASE = API_V1 + "/performance-comparison";
        public static final String COMPARE = "/{fundCode}";
    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class MacroData {
        public static final String BASE = API_V1 + "/macro-data";
        public static final String SYNC = "/sync";
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
