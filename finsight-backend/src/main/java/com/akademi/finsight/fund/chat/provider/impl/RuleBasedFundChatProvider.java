package com.akademi.finsight.fund.chat.provider.impl;

import com.akademi.finsight.fund.chat.dto.FundChatContext;
import com.akademi.finsight.fund.chat.dto.FundChatPrompt;
import com.akademi.finsight.fund.chat.dto.FundChatReply;
import com.akademi.finsight.fund.chat.dto.FundChatSource;
import com.akademi.finsight.fund.chat.knowledge.FundChatFaqEntry;
import com.akademi.finsight.fund.chat.knowledge.FundChatKnowledgeBase;
import com.akademi.finsight.fund.chat.provider.FundChatProvider;
import com.akademi.finsight.fund.dto.response.FundDashboardResponse;
import com.akademi.finsight.fund.dto.response.FundStockWeightResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "fund-chat", name = "provider", havingValue = "rule", matchIfMissing = true)
public class RuleBasedFundChatProvider implements FundChatProvider {

    private static final Locale TURKISH = Locale.of("tr");
    private static final Pattern DAY_PATTERN = Pattern.compile("(\\d{1,3})\\s*(gun|gunluk|g)\\b");
    private static final List<String> KNOWLEDGE_FIRST_MARKERS = List.of(
            "nedir", "ne demek", "ne anlama", "aciklar misin", "aciklayabilir",
            "degistir", "simule", "senaryo", "tavsiye", "onerir");
    private static final String DAILY_RETURN_INTENT = "daily-return";
    private static final int DISPLAY_SCALE = 2;
    private static final int TOP_STOCK_COUNT = 5;

    private final FundChatKnowledgeBase knowledgeBase;

    private final List<Intent> intents = List.of(
            new Intent("data-date",
                    List.of("veri tarihi", "hangi tarihe", "hangi tarih", "ne kadar guncel", "guncel mi"),
                    this::answerDataDate),
            new Intent("benchmark",
                    List.of("benchmark", "endeks", "karsilastirma", "bps", "gosterge"),
                    this::answerBenchmark),
            new Intent(DAILY_RETURN_INTENT,
                    List.of("gunluk getiri", "gunluk degisim", "bugun ne kadar", "bugunku getiri"),
                    this::answerDailyReturn),
            new Intent("total-value",
                    List.of("toplam deger", "portfoy degeri", "buyukluk", "kac tl", "toplam buyukluk"),
                    this::answerTotalValue),
            new Intent("stocks",
                    List.of("hangi hisse", "hisseler", "hisse agirlik", "en cok hisse"),
                    this::answerStocks),
            new Intent("distribution",
                    List.of("dagilim", "varlik dagilimi", "portfoy dagilimi", "neye yatirim"),
                    this::answerDistribution),
            new Intent("period-return",
                    List.of("getiri", "kumulatif", "performans", "kazanc", "ne kadar kazandirdi"),
                    this::answerPeriodReturn)
    );

    @Override
    public FundChatReply generate(FundChatPrompt prompt) {
        String question = normalize(prompt.userMessage());

        if (isKnowledgeFirstQuestion(question)) {
            Optional<FundChatReply> knowledge = knowledgeReply(question);
            if (knowledge.isPresent()) {
                return knowledge.get();
            }
        }

        Optional<FundChatReply> fromData = bestIntent(question)
                .map(intent -> FundChatReply.of(intent.answer().apply(question, prompt.context()), FundChatSource.RULE));

        if (fromData.isPresent()) {
            return fromData.get();
        }

        return knowledgeReply(question)
                .orElseGet(() -> {
                    log.debug("Fund chat question matched no intent or FAQ entry: fundCode={}", prompt.fundCode());
                    return FundChatReply.of(knowledgeBase.fallbackAnswer(), FundChatSource.FALLBACK);
                });
    }

    private boolean isKnowledgeFirstQuestion(String question) {
        return KNOWLEDGE_FIRST_MARKERS.stream().anyMatch(question::contains);
    }

    private Optional<FundChatReply> knowledgeReply(String question) {
        return bestFaqEntry(question)
                .map(entry -> FundChatReply.of(entry.answer(), FundChatSource.KNOWLEDGE));
    }

    private Optional<Intent> bestIntent(String question) {
        boolean periodSpecified = DAY_PATTERN.matcher(question).find();

        return intents.stream()
                .filter(intent -> !(periodSpecified && DAILY_RETURN_INTENT.equals(intent.id())))
                .filter(intent -> intent.score(question) > 0)
                .max(Comparator.comparingInt(intent -> intent.score(question)));
    }

    private Optional<FundChatFaqEntry> bestFaqEntry(String question) {
        return knowledgeBase.faqEntries().stream()
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

    private String answerDataDate(String question, FundChatContext context) {
        FundDashboardResponse dashboard = context.dashboard();
        return """
                %s fonunun ekranda gördüğünüz verileri %s tarihine ait.
                Fon verileri Infina'dan gecikmeli geldiği için en güncel iş gününü değil, \
                veri sağlayıcının yayınladığı son değerleme gününü gösteriyoruz."""
                .formatted(dashboard.fund().code(), dashboard.fund().dataDate());
    }

    private String answerTotalValue(String question, FundChatContext context) {
        FundDashboardResponse dashboard = context.dashboard();
        return "%s fonunun %s tarihli toplam portföy değeri %s TL.".formatted(
                dashboard.fund().code(),
                dashboard.fund().dataDate(),
                money(dashboard.totalValue()));
    }

    private String answerDailyReturn(String question, FundChatContext context) {
        FundDashboardResponse dashboard = context.dashboard();
        BigDecimal daily = dashboard.dailyReturn();
        return "%s fonunun %s tarihli günlük getirisi %%%s (%s).".formatted(
                dashboard.fund().code(),
                dashboard.fund().dataDate(),
                percent(daily),
                direction(daily));
    }

    private String answerPeriodReturn(String question, FundChatContext context) {
        FundDashboardResponse dashboard = context.dashboard();
        FundDashboardResponse.PeriodMetrics period = resolvePeriod(question, dashboard);

        if (period == null) {
            return knowledgeBase.fallbackAnswer();
        }

        return "%s fonunun %s dönemindeki kümülatif getirisi %%%s (%s tarihinden %s tarihine, %s)."
                .formatted(
                        dashboard.fund().code(),
                        period.code(),
                        percent(period.cumulativeReturn()),
                        period.previousDate(),
                        dashboard.fund().dataDate(),
                        direction(period.cumulativeReturn()));
    }

    private String answerBenchmark(String question, FundChatContext context) {
        FundDashboardResponse dashboard = context.dashboard();
        FundDashboardResponse.PeriodMetrics period = resolvePeriod(question, dashboard);

        if (period == null || period.benchmarkDiffBps() == null) {
            return knowledgeBase.fallbackAnswer();
        }

        boolean above = period.benchmarkDiffBps().signum() >= 0;
        return """
                %s döneminde fon %%%s, benchmark %%%s getirmiş. \
                Fon benchmark'ın %s bps %s."""
                .formatted(
                        period.code(),
                        percent(period.cumulativeReturn()),
                        percent(period.benchmarkReturn()),
                        percent(period.benchmarkDiffBps().abs()),
                        above ? "üzerinde" : "altında");
    }

    private String answerDistribution(String question, FundChatContext context) {
        FundDashboardResponse dashboard = context.dashboard();

        if (dashboard.distribution().isEmpty()) {
            return knowledgeBase.fallbackAnswer();
        }

        String breakdown = dashboard.distribution().stream()
                .map(category -> "%s %%%s".formatted(category.category(), percent(category.weight())))
                .collect(Collectors.joining(", "));

        return "%s fonunun %s tarihli varlık dağılımı: %s.".formatted(
                dashboard.fund().code(), dashboard.fund().dataDate(), breakdown);
    }

    private String answerStocks(String question, FundChatContext context) {
        FundDashboardResponse dashboard = context.dashboard();
        List<FundStockWeightResponse> items = dashboard.stockBreakdown().items();

        if (items.isEmpty()) {
            return knowledgeBase.fallbackAnswer();
        }

        String top = items.stream()
                .filter(item -> item.weight() != null)
                .sorted(Comparator.comparing(FundStockWeightResponse::weight, Comparator.reverseOrder()))
                .limit(TOP_STOCK_COUNT)
                .map(item -> "%s %%%s".formatted(item.assetCode(), percent(item.weight())))
                .collect(Collectors.joining(", "));

        return "%s dönemi itibarıyla en yüksek ağırlıklı hisseler: %s.".formatted(
                dashboard.stockBreakdown().period(), top);
    }

    private FundDashboardResponse.PeriodMetrics resolvePeriod(String question, FundDashboardResponse dashboard) {
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

    private static String percent(BigDecimal value) {
        return value == null ? "-" : value.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    private static String money(BigDecimal value) {
        return value == null ? "-" : value.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    private static String direction(BigDecimal value) {
        if (value == null || value.signum() == 0) {
            return "değişim yok";
        }
        return value.signum() > 0 ? "pozitif" : "negatif";
    }

    private record Intent(String id, List<String> keywords, BiFunction<String, FundChatContext, String> answer) {

        int score(String question) {
            return hits(question, keywords);
        }
    }
}
