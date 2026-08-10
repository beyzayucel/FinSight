package com.akademi.finsight.fund.chat.provider.impl;

import com.akademi.finsight.fund.chat.dto.FundChatContext;
import com.akademi.finsight.fund.chat.dto.FundChatPrompt;
import com.akademi.finsight.fund.chat.dto.FundChatReply;
import com.akademi.finsight.fund.chat.dto.FundChatSource;
import com.akademi.finsight.fund.chat.knowledge.FundChatFaqEntry;
import com.akademi.finsight.fund.chat.knowledge.FundChatKnowledgeBase;
import com.akademi.finsight.fund.dto.response.FundDashboardResponse;
import com.akademi.finsight.fund.dto.response.FundStockBreakdownResponse;
import com.akademi.finsight.fund.dto.response.FundStockWeightResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RuleBasedFundChatProvider")
class RuleBasedFundChatProviderTest {

    private static final String FUND_CODE = "TIE";
    private static final LocalDate DATA_DATE = LocalDate.of(2026, Month.JULY, 31);
    private static final String FALLBACK = "Bu soruyu cevaplayacak veriye sahip değilim.";

    @Mock
    private FundChatKnowledgeBase knowledgeBase;

    @InjectMocks
    private RuleBasedFundChatProvider provider;

    @BeforeEach
    void setUp() {
        when(knowledgeBase.fallbackAnswer()).thenReturn(FALLBACK);
        when(knowledgeBase.faqEntries()).thenReturn(List.of(
                new FundChatFaqEntry("bps-explained",
                        List.of("baz puan nedir", "bps nedir"),
                        "Baz puan, yüzdenin yüzde biridir."),
                new FundChatFaqEntry("benchmark-explained",
                        List.of("benchmark nedir", "endeks nedir"),
                        "Benchmark, fonun kıyaslandığı referans endekstir."),
                new FundChatFaqEntry("cumulative-explained",
                        List.of("kumulatif getiri nedir", "kumulatif ne demek"),
                        "Kümülatif getiri, dönem başından biriken toplam yüzde getiridir."),
                new FundChatFaqEntry("no-advice",
                        List.of("alayim mi", "tavsiye", "ne onerirsin"),
                        "Yatırım tavsiyesi veremem; \"AI Önerisi & Karar\" sayfasına gidebilirsin."),
                new FundChatFaqEntry("scenario-redirect",
                        List.of("senaryo", "simulasyon", "dagilimi degistir", "agirlik degistir"),
                        "Senaryo kurmak için \"AI Önerisi & Karar\" sayfasını kullan.")));
    }

    private FundChatReply ask(String message) {
        return provider.generate(new FundChatPrompt(
                FUND_CODE, "system", "glossary", context(), List.of(), message));
    }

    private FundChatContext context() {
        FundDashboardResponse dashboard = new FundDashboardResponse(
                new FundDashboardResponse.FundInfo(FUND_CODE, "Test Fonu", DATA_DATE),
                new BigDecimal("1056679.1234"),
                new BigDecimal("0.150000"),
                List.of(
                        period("P10D", "1.100000", "0.900000", "20.00"),
                        period("P30D", "5.670000", "4.200000", "147.00")),
                List.of(new FundDashboardResponse.CategoryWeight("Hisse Senedi", new BigDecimal("45.500000"))),
                new FundStockBreakdownResponse("2026-07", List.of(
                        new FundStockWeightResponse("ASELS", new BigDecimal("13.440000")),
                        new FundStockWeightResponse("THYAO", new BigDecimal("9.100000")))));

        return new FundChatContext(dashboard, "summary");
    }

    private FundDashboardResponse.PeriodMetrics period(String code,
                                                       String cumulative,
                                                       String benchmark,
                                                       String diffBps) {
        return new FundDashboardResponse.PeriodMetrics(
                code, new BigDecimal("1000000"), DATA_DATE.minusDays(30), 30,
                new BigDecimal("56679"), new BigDecimal("5.67"),
                new BigDecimal(cumulative), new BigDecimal(benchmark), new BigDecimal(diffBps),
                List.of());
    }

    @Nested
    @DisplayName("dashboard-backed answers")
    class DataAnswers {

        @DisplayName("should answer from live dashboard data")
        @ParameterizedTest(name = "\"{0}\" mentions {1}")
        @CsvSource(delimiter = '|', textBlock = """
                Günlük getiri ne kadar?           | 0.15
                GÜNLÜK GETİRİ NE KADAR?           | 0.15
                Son 30 günlük getirisi ne oldu?   | P30D
                Son 30 günlük getirisi ne oldu?   | 5.67
                Bu fon ne kadar kazandırdı?       | P10D
                Benchmark ile arasındaki fark ne? | üzerinde
                Hangi hisseler var?               | ASELS
                Veri tarihi hangi gün?            | 2026-07-31
                Portföy dağılımı nasıl?           | Hisse Senedi
                Toplam değer nedir?               | 1056679
                """)
        void shouldAnswerFromData(String question, String expectedFragment) {
            FundChatReply reply = ask(question);

            assertEquals(FundChatSource.RULE, reply.source());
            assertTrue(reply.text().contains(expectedFragment), reply.text());
        }
    }

    @Nested
    @DisplayName("knowledge, redirects and fallback")
    class KnowledgeAnswers {

        @DisplayName("should answer from the FAQ file instead of the data intents")
        @ParameterizedTest(name = "\"{0}\" mentions {1}")
        @CsvSource(delimiter = '|', textBlock = """
                Baz puan nedir?                | yüzdenin yüzde biridir
                bps nedir?                     | yüzdenin yüzde biridir
                Benchmark nedir?               | referans endekstir
                Kümülatif getiri nedir?        | biriken toplam
                Sence ne önerirsin, alayım mı? | AI Önerisi & Karar
                Dağılımı değiştirebilir miyim? | AI Önerisi & Karar
                """)
        void shouldAnswerFromKnowledge(String question, String expectedFragment) {
            FundChatReply reply = ask(question);

            assertEquals(FundChatSource.KNOWLEDGE, reply.source());
            assertTrue(reply.text().contains(expectedFragment), reply.text());
        }

        @Test
        @DisplayName("should return the configured fallback when nothing matches")
        void shouldReturnFallback() {
            FundChatReply reply = ask("Yarın hava nasıl olacak?");

            assertEquals(FundChatSource.FALLBACK, reply.source());
            assertEquals(FALLBACK, reply.text());
        }
    }
}
