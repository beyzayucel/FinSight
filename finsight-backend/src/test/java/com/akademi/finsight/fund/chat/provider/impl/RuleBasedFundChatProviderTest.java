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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RuleBasedFundChatProvider")
class RuleBasedFundChatProviderTest {

    private static final String FUND_CODE = "TIE";
    private static final LocalDate DATA_DATE = LocalDate.of(2026, 7, 31);
    private static final String FALLBACK = "Bu soruyu cevaplayacak veriye sahip değilim.";

    @Mock
    private FundChatKnowledgeBase knowledgeBase;

    @InjectMocks
    private RuleBasedFundChatProvider provider;

    @BeforeEach
    void setUp() {
        lenient().when(knowledgeBase.fallbackAnswer()).thenReturn(FALLBACK);
        lenient().when(knowledgeBase.faqEntries()).thenReturn(List.of(
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

        @Test
        @DisplayName("should answer the daily return from live data")
        void shouldAnswerDailyReturn() {
            FundChatReply reply = ask("Günlük getiri ne kadar?");

            assertEquals(FundChatSource.RULE, reply.source());
            assertTrue(reply.text().contains("0.15"), reply.text());
        }

        @Test
        @DisplayName("should pick the period named in the question instead of the daily return")
        void shouldPreferNamedPeriodOverDailyReturn() {
            FundChatReply reply = ask("Son 30 günlük getirisi ne oldu?");

            assertEquals(FundChatSource.RULE, reply.source());
            assertTrue(reply.text().contains("P30D"), reply.text());
            assertTrue(reply.text().contains("5.67"), reply.text());
        }

        @Test
        @DisplayName("should fall back to the first period when no day count is given")
        void shouldUseFirstPeriodWithoutDayCount() {
            FundChatReply reply = ask("Bu fon ne kadar kazandırdı?");

            assertEquals(FundChatSource.RULE, reply.source());
            assertTrue(reply.text().contains("P10D"), reply.text());
        }

        @Test
        @DisplayName("should report the benchmark gap with a direction")
        void shouldAnswerBenchmark() {
            FundChatReply reply = ask("Benchmark ile arasındaki fark ne?");

            assertEquals(FundChatSource.RULE, reply.source());
            assertTrue(reply.text().contains("üzerinde"), reply.text());
        }

        @Test
        @DisplayName("should list the heaviest stocks")
        void shouldAnswerStocks() {
            FundChatReply reply = ask("Hangi hisseler var?");

            assertEquals(FundChatSource.RULE, reply.source());
            assertTrue(reply.text().contains("ASELS"), reply.text());
        }

        @Test
        @DisplayName("should explain the data date")
        void shouldAnswerDataDate() {
            FundChatReply reply = ask("Veri tarihi hangi gün?");

            assertEquals(FundChatSource.RULE, reply.source());
            assertTrue(reply.text().contains("2026-07-31"), reply.text());
        }

        @Test
        @DisplayName("should match keywords regardless of Turkish casing")
        void shouldNormalizeTurkishCasing() {
            FundChatReply reply = ask("GÜNLÜK GETİRİ NE KADAR?");

            assertEquals(FundChatSource.RULE, reply.source());
            assertTrue(reply.text().contains("0.15"), reply.text());
        }
    }

    @Nested
    @DisplayName("knowledge and fallback")
    class KnowledgeAnswers {

        @Test
        @DisplayName("should answer definition questions from the FAQ file")
        void shouldAnswerFromFaq() {
            FundChatReply reply = ask("Baz puan nedir?");

            assertEquals(FundChatSource.KNOWLEDGE, reply.source());
            assertTrue(reply.text().contains("yüzdenin yüzde biridir"), reply.text());
        }

        @Test
        @DisplayName("should prefer the FAQ definition over the benchmark data intent")
        void shouldPreferDefinitionOverBenchmarkIntent() {
            FundChatReply reply = ask("Benchmark nedir?");

            assertEquals(FundChatSource.KNOWLEDGE, reply.source());
            assertTrue(reply.text().contains("referans endekstir"), reply.text());
        }

        @Test
        @DisplayName("should prefer the FAQ definition over the period return intent")
        void shouldPreferDefinitionOverPeriodIntent() {
            FundChatReply reply = ask("Kümülatif getiri nedir?");

            assertEquals(FundChatSource.KNOWLEDGE, reply.source());
            assertTrue(reply.text().contains("biriken toplam"), reply.text());
        }

        @Test
        @DisplayName("should answer bps definitions in both phrasings")
        void shouldAnswerBpsInBothPhrasings() {
            assertEquals(FundChatSource.KNOWLEDGE, ask("bps nedir?").source());
            assertEquals(FundChatSource.KNOWLEDGE, ask("Baz puan nedir?").source());
        }

        @Test
        @DisplayName("should fall through to data when a definition question has no FAQ entry")
        void shouldFallThroughToDataForUnknownDefinition() {
            FundChatReply reply = ask("Toplam değer nedir?");

            assertEquals(FundChatSource.RULE, reply.source());
            assertTrue(reply.text().contains("1056679"), reply.text());
        }

        @Test
        @DisplayName("should redirect advice questions to the AI decision page")
        void shouldRedirectAdviceQuestions() {
            FundChatReply reply = ask("Sence ne önerirsin, alayım mı?");

            assertEquals(FundChatSource.KNOWLEDGE, reply.source());
            assertTrue(reply.text().contains("AI Önerisi & Karar"), reply.text());
        }

        @Test
        @DisplayName("should redirect scenario questions instead of answering with the distribution")
        void shouldRedirectScenarioQuestions() {
            FundChatReply reply = ask("Dağılımı değiştirebilir miyim?");

            assertEquals(FundChatSource.KNOWLEDGE, reply.source());
            assertTrue(reply.text().contains("AI Önerisi & Karar"), reply.text());
        }

        @Test
        @DisplayName("should still answer a plain distribution question with data")
        void shouldKeepPlainDistributionOnData() {
            FundChatReply reply = ask("Portföy dağılımı nasıl?");

            assertEquals(FundChatSource.RULE, reply.source());
            assertTrue(reply.text().contains("Hisse Senedi"), reply.text());
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
