package com.akademi.finsight.fund.chat.context;

import com.akademi.finsight.fund.chat.dto.FundChatContext;
import com.akademi.finsight.fund.dto.response.FundDashboardResponse;
import com.akademi.finsight.fund.dto.response.FundStockWeightResponse;
import com.akademi.finsight.fund.service.FundDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

@Component
@RequiredArgsConstructor
public class FundChatContextBuilder {

    private static final int TOP_STOCK_COUNT = 5;

    private final FundDashboardService fundDashboardService;

    public FundChatContext build(String fundCode) {
        FundDashboardResponse dashboard = fundDashboardService.getDashboard(fundCode);
        return new FundChatContext(dashboard, render(dashboard));
    }

    private String render(FundDashboardResponse dashboard) {
        StringJoiner text = new StringJoiner(System.lineSeparator());

        text.add("Fund: %s (%s)".formatted(dashboard.fund().name(), dashboard.fund().code()));
        text.add("Data date: %s".formatted(dashboard.fund().dataDate()));
        text.add("Total portfolio value: %s TRY".formatted(dashboard.totalValue()));
        text.add("Daily return: %%%s".formatted(dashboard.dailyReturn()));

        text.add("");
        text.add("Period returns:");
        dashboard.periods().forEach(period -> text.add(renderPeriod(period)));

        text.add("");
        text.add("Asset allocation:");
        dashboard.distribution().forEach(category ->
                text.add("- %s: %%%s".formatted(category.category(), category.weight())));

        text.add("");
        text.add("Top %d stock positions (%s disclosure period):"
                .formatted(TOP_STOCK_COUNT, dashboard.stockBreakdown().period()));
        topStocks(dashboard).forEach(stock ->
                text.add("- %s: %%%s".formatted(stock.assetCode(), stock.weight())));

        return text.toString();
    }

    private String renderPeriod(FundDashboardResponse.PeriodMetrics period) {
        return "- %s: cumulative %%%s, benchmark %%%s, gap %s bps (since %s)".formatted(
                period.code(),
                period.cumulativeReturn(),
                period.benchmarkReturn(),
                period.benchmarkDiffBps(),
                period.previousDate());
    }

    private List<FundStockWeightResponse> topStocks(FundDashboardResponse dashboard) {
        return dashboard.stockBreakdown().items().stream()
                .filter(item -> item.weight() != null)
                .sorted(Comparator.comparing(FundStockWeightResponse::weight, Comparator.reverseOrder()))
                .limit(TOP_STOCK_COUNT)
                .toList();
    }
}
