package com.akademi.finsight.user.dto.response;

public record UserStatsResponse(
        long totalUsers,
        long activeUsers,
        long inactiveUsers,
        long todayLogins
){}
