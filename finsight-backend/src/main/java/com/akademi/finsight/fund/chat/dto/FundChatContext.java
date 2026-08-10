package com.akademi.finsight.fund.chat.dto;

import com.akademi.finsight.fund.dto.response.FundDashboardResponse;

public record FundChatContext(FundDashboardResponse dashboard, String summary) {}
