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

        text.add("Fon: %s (%s)".formatted(dashboard.fund().name(), dashboard.fund().code()));
        text.add("Veri tarihi: %s".formatted(dashboard.fund().dataDate()));
        text.add("Toplam portföy değeri: %s TL".formatted(dashboard.totalValue()));
        text.add("Günlük getiri: %%%s".formatted(dashboard.dailyReturn()));

        text.add("");
        text.add("Dönemsel getiriler:");
        dashboard.periods().forEach(period -> text.add(renderPeriod(period)));

        text.add("");
        text.add("Varlık dağılımı:");
        dashboard.distribution().forEach(category ->
                text.add("- %s: %%%s".formatted(category.category(), category.weight())));

        text.add("");
        text.add("En yüksek ağırlıklı %d hisse (%s dönemi):"
                .formatted(TOP_STOCK_COUNT, dashboard.stockBreakdown().period()));
        topStocks(dashboard).forEach(stock ->
                text.add("- %s: %%%s".formatted(stock.assetCode(), stock.weight())));

        return text.toString();
    }

    private String renderPeriod(FundDashboardResponse.PeriodMetrics period) {
        return "- %s: kümülatif %%%s, benchmark %%%s, fark %s bps (%s tarihinden bu yana)".formatted(
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
