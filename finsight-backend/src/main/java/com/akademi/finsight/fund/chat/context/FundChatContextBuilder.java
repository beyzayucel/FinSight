package com.akademi.finsight.fund.chat.context;

import com.akademi.finsight.fund.chat.dto.FundChatContext;
import com.akademi.finsight.fund.service.FundDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FundChatContextBuilder {

    private final FundDashboardService fundDashboardService;

    public FundChatContext build(String fundCode) {
        return new FundChatContext(fundDashboardService.getDashboard(fundCode));
    }
}
