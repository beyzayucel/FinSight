package com.akademi.finsight.fund.chat.provider.impl;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.fund.chat.config.FundChatProperties;
import com.akademi.finsight.fund.chat.dto.FundChatContext;
import com.akademi.finsight.fund.chat.dto.FundChatPrompt;
import com.akademi.finsight.fund.chat.dto.FundChatReply;
import com.akademi.finsight.fund.chat.dto.FundChatSource;
import com.akademi.finsight.fund.chat.knowledge.FundChatKnowledgeBase;
import com.akademi.finsight.fund.dto.response.FundDashboardResponse;
import com.akademi.finsight.fund.dto.response.FundStockBreakdownResponse;
import com.akademi.finsight.fund.dto.response.FundStockWeightResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RuleBasedFundChatProvider")
class RuleBasedFundChatProviderTest {

    private static final String FUND_CODE = "TIE";
    private static final LocalDate DATA_DATE = LocalDate.of(2026, Month.JULY, 31);

    private static FundChatKnowledgeBase knowledgeBase;

    private final RuleBasedFundChatProvider provider = new RuleBasedFundChatProvider();

    @BeforeAll
    static void loadShippedContent() {
        knowledgeBase = new FundChatKnowledgeBase(new FundChatProperties(), JsonMapper.builder().build());
        knowledgeBase.load();
    }

    private FundChatReply ask(SupportedLanguage language, String message) {
        return provider.generate(new FundChatPrompt(
                FUND_CODE, language, knowledgeBase.content(language), context(), List.of(), message));
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

        return new FundChatContext(dashboard);
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
    @DisplayName("shipped content")
    class ShippedContent {

        @DisplayName("should load every supported language with a usable fallback")
        @ParameterizedTest(name = "{0}")
        @EnumSource(SupportedLanguage.class)
        void shouldLoadEveryLanguage(SupportedLanguage language) {
            assertFalse(knowledgeBase.content(language).faq().fallback().isBlank());
            assertFalse(knowledgeBase.content(language).intents().entries().isEmpty());
        }
    }

    @Nested
    @DisplayName("Turkish")
    class Turkish {

        @DisplayName("should route Turkish questions")
        @ParameterizedTest(name = "\"{0}\" -> {1} mentions {2}")
        @CsvSource(delimiter = '|', textBlock = """
                Günlük getiri ne kadar?          | RULE      | 0.15
                Son 30 günlük getirisi ne oldu?  | RULE      | P30D
                Benchmark ile farkı ne?          | RULE      | üzerinde
                Hangi hisseler var?              | RULE      | ASELS
                Veri tarihi hangi gün?           | RULE      | 2026-07-31
                Portföy dağılımı nasıl?          | RULE      | Hisse Senedi
                Toplam değer nedir?              | RULE      | 1056679
                Benchmark nedir?                 | KNOWLEDGE | referans endeks
                Ne önerirsin?                    | KNOWLEDGE | AI Önerisi & Karar
                Dağılımı değiştirebilir miyim?   | KNOWLEDGE | AI Önerisi & Karar
                Yarın hava nasıl olacak?         | FALLBACK  | veriye sahip değilim
                """)
        void shouldRouteTurkishQuestion(String question, FundChatSource expectedSource, String expectedFragment) {
            FundChatReply reply = ask(SupportedLanguage.TR, question);

            assertEquals(expectedSource, reply.source());
            assertTrue(reply.text().contains(expectedFragment), reply.text());
        }
    }

    @Nested
    @DisplayName("English")
    class English {

        @DisplayName("should route English questions")
        @ParameterizedTest(name = "\"{0}\" -> {1} mentions {2}")
        @CsvSource(delimiter = '|', textBlock = """
                What was the daily return?                | RULE      | 0.15
                What is the return over the last 30 days? | RULE      | P30D
                Benchmark gap?                            | RULE      | above
                Which stocks does it hold?                | RULE      | ASELS
                What is the data date?                    | RULE      | 2026-07-31
                Portfolio allocation?                     | RULE      | Hisse Senedi
                What is the total value?                  | RULE      | 1056679
                What is the benchmark?                    | KNOWLEDGE | reference index
                Should I buy this fund?                   | KNOWLEDGE | AI Recommendation & Decision
                Can I change the allocation?              | KNOWLEDGE | AI Recommendation & Decision
                How is the weather tomorrow?              | FALLBACK  | don't have the data
                """)
        void shouldRouteEnglishQuestion(String question, FundChatSource expectedSource, String expectedFragment) {
            FundChatReply reply = ask(SupportedLanguage.EN, question);

            assertEquals(expectedSource, reply.source());
            assertTrue(reply.text().contains(expectedFragment), reply.text());
        }

        @Test
        @DisplayName("should answer in English while keeping data values untranslated")
        void shouldKeepDataValuesUntranslated() {
            FundChatReply reply = ask(SupportedLanguage.EN, "What is the total value?");

            assertTrue(reply.text().contains("TRY"), reply.text());
            assertTrue(reply.text().contains(FUND_CODE), reply.text());
        }
    }
}
