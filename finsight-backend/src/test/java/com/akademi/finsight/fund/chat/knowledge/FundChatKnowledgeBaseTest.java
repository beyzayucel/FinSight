package com.akademi.finsight.fund.chat.knowledge;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.fund.chat.config.FundChatProperties;
import com.akademi.finsight.fund.chat.exception.FundChatErrorType;
import com.akademi.finsight.fund.chat.exception.FundChatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("FundChatKnowledgeBase")
class FundChatKnowledgeBaseTest {

    private static final List<String> EXPECTED_INTENT_IDS = List.of(
            "data-date", "benchmark", "daily-return", "total-value",
            "stocks", "distribution", "period-return");

    private static final int DATA_DATE_PLACEHOLDERS = 2;

    @Mock
    private ObjectMapper objectMapper;

    private FundChatProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FundChatProperties();
        properties.setKnowledgePath("fund-chat");
    }

    private FundChatKnowledgeBase knowledgeBase() {
        return new FundChatKnowledgeBase(properties, objectMapper);
    }

    private void stub(FundChatFaq faq, FundChatIntents intents) {
        lenient().when(objectMapper.readValue(any(InputStream.class), eq(FundChatFaq.class))).thenReturn(faq);
        lenient().when(objectMapper.readValue(any(InputStream.class), eq(FundChatIntents.class))).thenReturn(intents);
    }

    private static FundChatFaq validFaq() {
        return new FundChatFaq("Bu soruyu cevaplayacak veriye sahip degilim.", List.of());
    }

    private static FundChatIntents validIntents() {
        return intentsWith(placeholders(DATA_DATE_PLACEHOLDERS));
    }

    private static FundChatIntents intentsWith(String dataDateTemplate) {
        List<FundChatIntentEntry> entries = new ArrayList<>();
        entries.add(new FundChatIntentEntry("data-date", List.of("veri tarihi"), dataDateTemplate));

        EXPECTED_INTENT_IDS.stream()
                .filter(id -> !"data-date".equals(id))
                .forEach(id -> entries.add(new FundChatIntentEntry(id, List.of(id), placeholderCountFor(id))));

        return new FundChatIntents(List.of("nedir"), vocabulary(), entries);
    }

    private static FundChatVocabulary vocabulary() {
        return new FundChatVocabulary("pozitif", "negatif", "degisim yok", "uzerinde", "altinda", "-");
    }

    private static String placeholders(int count) {
        return "%s ".repeat(count).trim();
    }

    private static String placeholderCountFor(String id) {
        return switch (id) {
            case "benchmark" -> placeholders(5);
            case "daily-return" -> placeholders(4);
            case "total-value", "distribution" -> placeholders(3);
            case "stocks" -> placeholders(2);
            case "period-return" -> placeholders(6);
            default -> placeholders(DATA_DATE_PLACEHOLDERS);
        };
    }

    private FundChatException assertLoadFails() {
        FundChatKnowledgeBase knowledgeBase = knowledgeBase();
        return assertThrows(FundChatException.class, knowledgeBase::load);
    }

    @Nested
    @DisplayName("valid content")
    class ValidContent {

        @DisplayName("should expose the loaded content for every supported language")
        @ParameterizedTest(name = "{0}")
        @EnumSource(SupportedLanguage.class)
        void shouldExposeContentPerLanguage(SupportedLanguage language) {
            FundChatFaq faq = validFaq();
            FundChatIntents intents = validIntents();
            stub(faq, intents);

            FundChatKnowledgeBase knowledgeBase = knowledgeBase();
            knowledgeBase.load();

            assertSame(faq, knowledgeBase.content(language).faq());
            assertSame(intents, knowledgeBase.content(language).intents());
        }

        @Test
        @DisplayName("should not count an escaped percent sign as a placeholder")
        void shouldIgnoreEscapedPercent() {
            stub(validFaq(), intentsWith("%%%s oraninda, %s tarihinde"));

            assertDoesNotThrow(() -> knowledgeBase().load());
        }
    }

    @Nested
    @DisplayName("unreadable files")
    class UnreadableFiles {

        @Test
        @DisplayName("should fail when the knowledge directory does not exist")
        void shouldFailWhenDirectoryMissing() {
            properties.setKnowledgePath("fund-chat-does-not-exist");

            assertEquals(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, assertLoadFails().getErrorType());
        }

        @Test
        @DisplayName("should fail when a file parses to null")
        void shouldFailWhenPayloadIsNull() {
            stub(null, validIntents());

            assertEquals(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, assertLoadFails().getErrorType());
        }
    }

    @Nested
    @DisplayName("invalid FAQ")
    class InvalidFaq {

        @DisplayName("should fail when the fallback answer is missing")
        @ParameterizedTest(name = "fallback=[{0}]")
        @ValueSource(strings = {"", "   "})
        void shouldFailOnBlankFallback(String fallback) {
            stub(new FundChatFaq(fallback, List.of()), validIntents());

            assertEquals(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, assertLoadFails().getErrorType());
        }

        @Test
        @DisplayName("should fail when the fallback answer is null")
        void shouldFailOnNullFallback() {
            stub(new FundChatFaq(null, List.of()), validIntents());

            assertEquals(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, assertLoadFails().getErrorType());
        }

        @Test
        @DisplayName("should fail when the entry list is missing")
        void shouldFailOnNullEntries() {
            stub(new FundChatFaq("fallback", null), validIntents());

            assertEquals(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, assertLoadFails().getErrorType());
        }
    }

    @Nested
    @DisplayName("invalid intents")
    class InvalidIntents {

        @Test
        @DisplayName("should fail when the vocabulary is missing")
        void shouldFailOnMissingVocabulary() {
            stub(validFaq(), new FundChatIntents(List.of(), null, validIntents().entries()));

            assertEquals(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, assertLoadFails().getErrorType());
        }

        @Test
        @DisplayName("should fail when the knowledge-first markers are missing")
        void shouldFailOnMissingMarkers() {
            stub(validFaq(), new FundChatIntents(null, vocabulary(), validIntents().entries()));

            assertEquals(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, assertLoadFails().getErrorType());
        }

        @Test
        @DisplayName("should fail when a known intent is not defined")
        void shouldFailOnMissingIntent() {
            List<FundChatIntentEntry> withoutStocks = validIntents().entries().stream()
                    .filter(entry -> !"stocks".equals(entry.id()))
                    .toList();
            stub(validFaq(), new FundChatIntents(List.of(), vocabulary(), withoutStocks));

            assertEquals(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, assertLoadFails().getErrorType());
        }

        @Test
        @DisplayName("should fail when the file defines an intent nobody can answer")
        void shouldFailOnUnknownIntent() {
            List<FundChatIntentEntry> withExtra = new ArrayList<>(validIntents().entries());
            withExtra.add(new FundChatIntentEntry("weather", List.of("hava"), placeholders(1)));
            stub(validFaq(), new FundChatIntents(List.of(), vocabulary(), withExtra));

            assertEquals(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, assertLoadFails().getErrorType());
        }

        @DisplayName("should fail when a template has the wrong number of placeholders")
        @ParameterizedTest(name = "data-date with {0} placeholders")
        @ValueSource(ints = {1, 3})
        void shouldFailOnPlaceholderMismatch(int count) {
            stub(validFaq(), intentsWith(placeholders(count)));

            assertEquals(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, assertLoadFails().getErrorType());
        }

        @Test
        @DisplayName("should fail when a template is missing entirely")
        void shouldFailOnNullTemplate() {
            stub(validFaq(), intentsWith(null));

            assertEquals(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, assertLoadFails().getErrorType());
        }
    }
}
