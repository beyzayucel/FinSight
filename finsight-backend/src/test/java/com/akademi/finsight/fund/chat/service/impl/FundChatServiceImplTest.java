package com.akademi.finsight.fund.chat.service.impl;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.fund.chat.context.FundChatContextBuilder;
import com.akademi.finsight.fund.chat.dto.FundChatContext;
import com.akademi.finsight.fund.chat.dto.FundChatPrompt;
import com.akademi.finsight.fund.chat.dto.FundChatReply;
import com.akademi.finsight.fund.chat.dto.FundChatRole;
import com.akademi.finsight.fund.chat.dto.FundChatSource;
import com.akademi.finsight.fund.chat.dto.FundChatTurn;
import com.akademi.finsight.fund.chat.dto.request.FundChatRequest;
import com.akademi.finsight.fund.chat.dto.response.FundChatResponse;
import com.akademi.finsight.fund.chat.exception.FundChatErrorType;
import com.akademi.finsight.fund.chat.exception.FundChatException;
import com.akademi.finsight.fund.chat.knowledge.FundChatContent;
import com.akademi.finsight.fund.chat.knowledge.FundChatFaq;
import com.akademi.finsight.fund.chat.knowledge.FundChatIntents;
import com.akademi.finsight.fund.chat.knowledge.FundChatKnowledgeBase;
import com.akademi.finsight.fund.chat.memory.FundChatMemoryStore;
import com.akademi.finsight.fund.chat.provider.FundChatProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FundChatServiceImpl")
class FundChatServiceImplTest {

    private static final String EMAIL = "melis@example.com";
    private static final String FUND_CODE = "TIE";
    private static final String SESSION_ID = "session-0001";
    private static final String QUESTION = "Günlük getiri ne kadar?";
    private static final String ANSWER = "TIE fonunun günlük getirisi %0.15.";

    private static final FundChatContent CONTENT = new FundChatContent(
            new FundChatFaq("fallback", List.of()),
            new FundChatIntents(List.of(), null, List.of()));

    private static final FundChatContext CONTEXT = new FundChatContext(null);

    @Mock
    private FundChatProvider fundChatProvider;

    @Mock
    private FundChatKnowledgeBase knowledgeBase;

    @Mock
    private FundChatContextBuilder contextBuilder;

    @Mock
    private FundChatMemoryStore memoryStore;

    @InjectMocks
    private FundChatServiceImpl service;

    @Captor
    private ArgumentCaptor<FundChatPrompt> promptCaptor;

    @Captor
    private ArgumentCaptor<List<FundChatTurn>> turnsCaptor;

    @BeforeEach
    void stubHappyPath() {
        lenient().when(knowledgeBase.content(any())).thenReturn(CONTENT);
        lenient().when(contextBuilder.build(FUND_CODE)).thenReturn(CONTEXT);
        lenient().when(memoryStore.load(eq(EMAIL), eq(FUND_CODE), anyString())).thenReturn(List.of());
        lenient().when(fundChatProvider.generate(any()))
                .thenReturn(FundChatReply.of(ANSWER, FundChatSource.RULE));

        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void clearLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    private FundChatResponse ask(FundChatRequest request) {
        return service.ask(EMAIL, FUND_CODE, request);
    }

    private FundChatPrompt capturePrompt() {
        verify(fundChatProvider).generate(promptCaptor.capture());
        return promptCaptor.getValue();
    }

    @Nested
    @DisplayName("session id")
    class SessionId {

        @DisplayName("should generate a new id when the request does not carry one")
        @ParameterizedTest(name = "requested=[{0}]")
        @ValueSource(strings = {"", "   "})
        void shouldGenerateIdWhenMissing(String requested) {
            FundChatResponse response = ask(new FundChatRequest(requested, QUESTION, "tr"));

            assertNotEquals(requested, response.sessionId());
            assertDoesNotThrow(() -> UUID.fromString(response.sessionId()));
        }

        @Test
        @DisplayName("should generate a new id when sessionId is null")
        void shouldGenerateIdWhenNull() {
            FundChatResponse response = ask(new FundChatRequest(null, QUESTION, "tr"));

            assertDoesNotThrow(() -> UUID.fromString(response.sessionId()));
        }

        @Test
        @DisplayName("should keep the supplied id and use it for both load and save")
        void shouldKeepSuppliedId() {
            FundChatResponse response = ask(new FundChatRequest(SESSION_ID, QUESTION, "tr"));

            assertEquals(SESSION_ID, response.sessionId());
            verify(memoryStore).load(EMAIL, FUND_CODE, SESSION_ID);
            verify(memoryStore).save(eq(EMAIL), eq(FUND_CODE), eq(SESSION_ID), any());
        }

        @Test
        @DisplayName("should reuse the generated id for load, save and the response")
        void shouldReuseGeneratedId() {
            FundChatResponse response = ask(new FundChatRequest(null, QUESTION, "tr"));

            verify(memoryStore).load(EMAIL, FUND_CODE, response.sessionId());
            verify(memoryStore).save(eq(EMAIL), eq(FUND_CODE), eq(response.sessionId()), any());
        }
    }

    @Nested
    @DisplayName("language resolution")
    class LanguageResolution {

        @DisplayName("should take the language from the request body regardless of case")
        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @CsvSource({"tr,TR", "TR,TR", "en,EN", "EN,EN"})
        void shouldPreferRequestLanguage(String requested, SupportedLanguage expected) {
            ask(new FundChatRequest(SESSION_ID, QUESTION, requested));

            assertEquals(expected, capturePrompt().language());
            verify(knowledgeBase).content(expected);
        }

        @DisplayName("should fall back to the Accept-Language locale when the body omits it")
        @ParameterizedTest(name = "locale {0} -> {1}")
        @CsvSource({"tr,TR", "en,EN"})
        void shouldFallBackToLocale(String locale, SupportedLanguage expected) {
            LocaleContextHolder.setLocale(Locale.forLanguageTag(locale));

            ask(new FundChatRequest(SESSION_ID, QUESTION, null));

            assertEquals(expected, capturePrompt().language());
        }

        @Test
        @DisplayName("should fall back to English for an unsupported locale")
        void shouldFallBackToEnglishForUnsupportedLocale() {
            LocaleContextHolder.setLocale(Locale.GERMAN);

            ask(new FundChatRequest(SESSION_ID, QUESTION, null));

            assertEquals(SupportedLanguage.EN, capturePrompt().language());
        }
    }

    @Nested
    @DisplayName("prompt")
    class Prompt {

        @Test
        @DisplayName("should carry the fund code, message, loaded content, context and history")
        void shouldCarryEverythingTheProviderNeeds() {
            List<FundChatTurn> history = List.of(FundChatTurn.user("önceki soru", Instant.EPOCH));
            when(memoryStore.load(EMAIL, FUND_CODE, SESSION_ID)).thenReturn(history);

            ask(new FundChatRequest(SESSION_ID, QUESTION, "tr"));

            FundChatPrompt prompt = capturePrompt();
            assertEquals(FUND_CODE, prompt.fundCode());
            assertEquals(QUESTION, prompt.userMessage());
            assertSame(CONTENT, prompt.content());
            assertSame(CONTEXT, prompt.context());
            assertEquals(history, prompt.history());
        }
    }

    @Nested
    @DisplayName("response and history")
    class ResponseAndHistory {

        @Test
        @DisplayName("should mirror the reply text and source")
        void shouldMirrorReply() {
            when(fundChatProvider.generate(any()))
                    .thenReturn(FundChatReply.of("bilmiyorum", FundChatSource.FALLBACK));

            FundChatResponse response = ask(new FundChatRequest(SESSION_ID, QUESTION, "tr"));

            assertEquals("bilmiyorum", response.reply());
            assertEquals(FundChatSource.FALLBACK, response.source());
        }

        @Test
        @DisplayName("should append the question and the answer in order, stamped with answeredAt")
        void shouldAppendBothTurns() {
            FundChatResponse response = ask(new FundChatRequest(SESSION_ID, QUESTION, "tr"));

            verify(memoryStore).save(eq(EMAIL), eq(FUND_CODE), eq(SESSION_ID), turnsCaptor.capture());
            List<FundChatTurn> saved = turnsCaptor.getValue();

            assertEquals(2, saved.size());
            assertEquals(FundChatRole.USER, saved.get(0).role());
            assertEquals(QUESTION, saved.get(0).content());
            assertEquals(FundChatRole.ASSISTANT, saved.get(1).role());
            assertEquals(ANSWER, saved.get(1).content());
            assertEquals(response.answeredAt(), saved.get(0).at());
            assertEquals(response.answeredAt(), saved.get(1).at());
        }

        @Test
        @DisplayName("should keep the existing history and append after it")
        void shouldKeepExistingHistory() {
            FundChatTurn earlier = FundChatTurn.user("önceki soru", Instant.EPOCH);
            when(memoryStore.load(EMAIL, FUND_CODE, SESSION_ID)).thenReturn(List.of(earlier));

            ask(new FundChatRequest(SESSION_ID, QUESTION, "tr"));

            verify(memoryStore).save(eq(EMAIL), eq(FUND_CODE), eq(SESSION_ID), turnsCaptor.capture());
            List<FundChatTurn> saved = turnsCaptor.getValue();

            assertEquals(3, saved.size());
            assertEquals(earlier, saved.getFirst());
        }
    }

    @Nested
    @DisplayName("provider failures")
    class ProviderFailures {

        @Test
        @DisplayName("should wrap an unexpected runtime failure into FUND_CHAT_PROVIDER_FAILED and keep the cause")
        void shouldWrapRuntimeFailure() {
            RuntimeException cause = new IllegalStateException("boom");
            when(fundChatProvider.generate(any())).thenThrow(cause);
            FundChatRequest request = new FundChatRequest(SESSION_ID, QUESTION, "tr");

            FundChatException exception = assertThrows(FundChatException.class, () -> ask(request));

            assertEquals(FundChatErrorType.FUND_CHAT_PROVIDER_FAILED, exception.getErrorType());
            assertSame(cause, exception.getCause());
        }

        @Test
        @DisplayName("should rethrow a FundChatException without rewrapping it")
        void shouldRethrowFundChatException() {
            FundChatException original =
                    new FundChatException(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE);
            when(fundChatProvider.generate(any())).thenThrow(original);
            FundChatRequest request = new FundChatRequest(SESSION_ID, QUESTION, "tr");

            FundChatException exception = assertThrows(FundChatException.class, () -> ask(request));

            assertSame(original, exception);
        }

        @Test
        @DisplayName("should not store anything when the provider fails")
        void shouldNotStoreOnFailure() {
            when(fundChatProvider.generate(any())).thenThrow(new IllegalStateException("boom"));
            FundChatRequest request = new FundChatRequest(SESSION_ID, QUESTION, "tr");

            assertThrows(FundChatException.class, () -> ask(request));

            verify(memoryStore, never()).save(anyString(), anyString(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("reset")
    class Reset {

        @Test
        @DisplayName("should clear exactly the requested session")
        void shouldClearSession() {
            service.reset(EMAIL, FUND_CODE, SESSION_ID);

            verify(memoryStore).clear(EMAIL, FUND_CODE, SESSION_ID);
            verify(memoryStore, never()).load(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("should not reach the provider")
        void shouldNotReachProvider() {
            service.reset(EMAIL, FUND_CODE, SESSION_ID);

            verify(fundChatProvider, never()).generate(any());
        }
    }
}
