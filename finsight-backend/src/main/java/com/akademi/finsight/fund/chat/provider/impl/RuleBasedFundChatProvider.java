package com.akademi.finsight.fund.chat.provider.impl;

import com.akademi.finsight.fund.chat.dto.FundChatContext;
import com.akademi.finsight.fund.chat.dto.FundChatPrompt;
import com.akademi.finsight.fund.chat.dto.FundChatReply;
import com.akademi.finsight.fund.chat.dto.FundChatSource;
import com.akademi.finsight.fund.chat.knowledge.FundChatContent;
import com.akademi.finsight.fund.chat.knowledge.FundChatFaqEntry;
import com.akademi.finsight.fund.chat.knowledge.FundChatIntentEntry;
import com.akademi.finsight.fund.chat.knowledge.FundChatVocabulary;
import com.akademi.finsight.fund.chat.provider.FundChatProvider;
import com.akademi.finsight.fund.dto.response.FundDashboardResponse;
import com.akademi.finsight.fund.dto.response.FundStockWeightResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "fund-chat", name = "provider", havingValue = "rule", matchIfMissing = true)
public class RuleBasedFundChatProvider implements FundChatProvider {

    private static final Locale TURKISH = Locale.of("tr");
    private static final Pattern DAY_PATTERN =
            Pattern.compile("(\\d{1,3})\\s*(gunluk|gun|days|day|g|d)\\b");
    private static final String DAILY_RETURN_INTENT = "daily-return";
    private static final int DISPLAY_SCALE = 2;
    private static final int TOP_STOCK_COUNT = 5;

    private static final Map<String, AnswerBuilder> ANSWER_BUILDERS = Map.of(
            "data-date", RuleBasedFundChatProvider::answerDataDate,
            "benchmark", RuleBasedFundChatProvider::answerBenchmark,
            DAILY_RETURN_INTENT, RuleBasedFundChatProvider::answerDailyReturn,
            "total-value", RuleBasedFundChatProvider::answerTotalValue,
            "stocks", RuleBasedFundChatProvider::answerStocks,
            "distribution", RuleBasedFundChatProvider::answerDistribution,
            "period-return", RuleBasedFundChatProvider::answerPeriodReturn);

    @Override
    public FundChatReply generate(FundChatPrompt prompt) {
        String question = normalize(prompt.userMessage());
        FundChatContent content = prompt.content();

        if (isKnowledgeFirstQuestion(question, content)) {
            Optional<FundChatReply> knowledge = knowledgeReply(question, content);
            if (knowledge.isPresent()) {
                return knowledge.get();
            }
        }

        Optional<FundChatReply> fromData = bestIntent(question, content)
                .map(entry -> FundChatReply.of(
                        ANSWER_BUILDERS.get(entry.id()).build(question, prompt.context(), content, entry.template()),
                        FundChatSource.RULE));

        if (fromData.isPresent()) {
            return fromData.get();
        }

        return knowledgeReply(question, content)
                .orElseGet(() -> {
                    log.debug("Fund chat question matched no intent or FAQ entry: fundCode={}, language={}",
                            prompt.fundCode(), prompt.language());
                    return FundChatReply.of(content.faq().fallback(), FundChatSource.FALLBACK);
                });
    }

    private boolean isKnowledgeFirstQuestion(String question, FundChatContent content) {
        return content.intents().knowledgeFirstMarkers().stream()
                .map(RuleBasedFundChatProvider::normalize)
                .anyMatch(question::contains);
    }

    private Optional<FundChatReply> knowledgeReply(String question, FundChatContent content) {
        return bestFaqEntry(question, content)
                .map(entry -> FundChatReply.of(entry.answer(), FundChatSource.KNOWLEDGE));
    }

    private Optional<FundChatIntentEntry> bestIntent(String question, FundChatContent content) {
        boolean periodSpecified = DAY_PATTERN.matcher(question).find();

        return content.intents().entries().stream()
                .filter(entry -> ANSWER_BUILDERS.containsKey(entry.id()))
                .filter(entry -> !(periodSpecified && DAILY_RETURN_INTENT.equals(entry.id())))
                .filter(entry -> hits(question, entry.keywords()) > 0)
                .max(Comparator.comparingInt(entry -> hits(question, entry.keywords())));
    }

    private Optional<FundChatFaqEntry> bestFaqEntry(String question, FundChatContent content) {
        return content.faq().entries().stream()
                .filter(entry -> hits(question, entry.keywords()) > 0)
                .max(Comparator.comparingInt(entry -> hits(question, entry.keywords())));
    }

    private static int hits(String question, List<String> keywords) {
        return (int) keywords.stream()
                .map(RuleBasedFundChatProvider::normalize)
                .filter(question::contains)
                .count();
    }

    private static String normalize(String text) {
        return text.toLowerCase(TURKISH)
                .replace('ı', 'i')
                .replace('ğ', 'g')
                .replace('ü', 'u')
                .replace('ş', 's')
                .replace('ö', 'o')
                .replace('ç', 'c');
    }

    private static String answerDataDate(String question, FundChatContext context,
                                         FundChatContent content, String template) {
        FundDashboardResponse dashboard = context.dashboard();
        return template.formatted(dashboard.fund().code(), dashboard.fund().dataDate());
    }

    private static String answerTotalValue(String question, FundChatContext context,
                                           FundChatContent content, String template) {
        FundDashboardResponse dashboard = context.dashboard();
        return template.formatted(
                dashboard.fund().code(),
                dashboard.fund().dataDate(),
                amount(dashboard.totalValue(), content));
    }

    private static String answerDailyReturn(String question, FundChatContext context,
                                            FundChatContent content, String template) {
        FundDashboardResponse dashboard = context.dashboard();
        BigDecimal daily = dashboard.dailyReturn();
        return template.formatted(
                dashboard.fund().code(),
                dashboard.fund().dataDate(),
                amount(daily, content),
                direction(daily, content));
    }

    private static String answerPeriodReturn(String question, FundChatContext context,
                                             FundChatContent content, String template) {
        FundDashboardResponse dashboard = context.dashboard();
        FundDashboardResponse.PeriodMetrics period = resolvePeriod(question, dashboard);

        if (period == null) {
            return content.faq().fallback();
        }

        return template.formatted(
                dashboard.fund().code(),
                period.code(),
                amount(period.cumulativeReturn(), content),
                period.previousDate(),
                dashboard.fund().dataDate(),
                direction(period.cumulativeReturn(), content));
    }

    private static String answerBenchmark(String question, FundChatContext context,
                                          FundChatContent content, String template) {
        FundDashboardResponse dashboard = context.dashboard();
        FundDashboardResponse.PeriodMetrics period = resolvePeriod(question, dashboard);

        if (period == null || period.benchmarkDiffBps() == null) {
            return content.faq().fallback();
        }

        FundChatVocabulary vocabulary = content.intents().vocabulary();
        boolean above = period.benchmarkDiffBps().signum() >= 0;

        return template.formatted(
                period.code(),
                amount(period.cumulativeReturn(), content),
                amount(period.benchmarkReturn(), content),
                amount(period.benchmarkDiffBps().abs(), content),
                above ? vocabulary.aboveBenchmark() : vocabulary.belowBenchmark());
    }

    private static String answerDistribution(String question, FundChatContext context,
                                             FundChatContent content, String template) {
        FundDashboardResponse dashboard = context.dashboard();

        if (dashboard.distribution().isEmpty()) {
            return content.faq().fallback();
        }

        String breakdown = dashboard.distribution().stream()
                .map(category -> "%s %%%s".formatted(category.category(), amount(category.weight(), content)))
                .collect(Collectors.joining(", "));

        return template.formatted(dashboard.fund().code(), dashboard.fund().dataDate(), breakdown);
    }

    private static String answerStocks(String question, FundChatContext context,
                                       FundChatContent content, String template) {
        FundDashboardResponse dashboard = context.dashboard();
        List<FundStockWeightResponse> items = dashboard.stockBreakdown().items();

        if (items.isEmpty()) {
            return content.faq().fallback();
        }

        String top = items.stream()
                .filter(item -> item.weight() != null)
                .sorted(Comparator.comparing(FundStockWeightResponse::weight, Comparator.reverseOrder()))
                .limit(TOP_STOCK_COUNT)
                .map(item -> "%s %%%s".formatted(item.assetCode(), amount(item.weight(), content)))
                .collect(Collectors.joining(", "));

        return template.formatted(dashboard.stockBreakdown().period(), top);
    }

    private static FundDashboardResponse.PeriodMetrics resolvePeriod(String question,
                                                                     FundDashboardResponse dashboard) {
        if (dashboard.periods().isEmpty()) {
            return null;
        }

        Matcher matcher = DAY_PATTERN.matcher(question);
        if (matcher.find()) {
            String code = "P%sD".formatted(matcher.group(1));
            return dashboard.periods().stream()
                    .filter(period -> code.equalsIgnoreCase(period.code()))
                    .findFirst()
                    .orElseGet(() -> dashboard.periods().getFirst());
        }

        return dashboard.periods().getFirst();
    }

    private static String amount(BigDecimal value, FundChatContent content) {
        return value == null
                ? content.intents().vocabulary().unknownValue()
                : value.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    private static String direction(BigDecimal value, FundChatContent content) {
        FundChatVocabulary vocabulary = content.intents().vocabulary();

        if (value == null || value.signum() == 0) {
            return vocabulary.unchanged();
        }

        return value.signum() > 0 ? vocabulary.positive() : vocabulary.negative();
    }

    @FunctionalInterface
    private interface AnswerBuilder {
        String build(String question, FundChatContext context, FundChatContent content, String template);
    }
}
