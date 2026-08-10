package com.akademi.finsight.fund.chat.knowledge;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.fund.chat.config.FundChatProperties;
import com.akademi.finsight.fund.chat.exception.FundChatErrorType;
import com.akademi.finsight.fund.chat.exception.FundChatException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundChatKnowledgeBase {

    private static final String FAQ_FILE = "faq.json";
    private static final String INTENTS_FILE = "intents.json";
    private static final String PLACEHOLDER = "%s";
    private static final String ESCAPED_PERCENT = "%%";

    private static final Map<String, Integer> EXPECTED_PLACEHOLDERS = Map.of(
            "data-date", 2,
            "benchmark", 5,
            "daily-return", 4,
            "total-value", 3,
            "stocks", 2,
            "distribution", 3,
            "period-return", 6);

    private final FundChatProperties properties;
    private final ObjectMapper objectMapper;

    private final Map<SupportedLanguage, FundChatContent> contentByLanguage =
            new EnumMap<>(SupportedLanguage.class);

    @PostConstruct
    public void load() {
        for (SupportedLanguage language : SupportedLanguage.values()) {
            contentByLanguage.put(language, loadContent(language));
        }

        log.info("Fund chat knowledge loaded: path={}, languages={}",
                properties.getKnowledgePath(), contentByLanguage.keySet());
    }

    public FundChatContent content(SupportedLanguage language) {
        FundChatContent content = contentByLanguage.get(language);

        if (content == null) {
            log.error("Fund chat content is missing for a supported language: language={}", language);
            throw new FundChatException(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE);
        }

        return content;
    }

    private FundChatContent loadContent(SupportedLanguage language) {
        FundChatFaq faq = readJson(language, FAQ_FILE, FundChatFaq.class);
        FundChatIntents intents = readJson(language, INTENTS_FILE, FundChatIntents.class);

        validateFaq(language, faq);
        validateIntents(language, intents);

        return new FundChatContent(faq, intents);
    }

    private void validateFaq(SupportedLanguage language, FundChatFaq faq) {
        if (faq.fallback() == null || faq.fallback().isBlank() || faq.entries() == null) {
            log.error("Fund chat FAQ is missing a fallback or an entry list: language={}", language);
            throw new FundChatException(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE);
        }
    }

    private void validateIntents(SupportedLanguage language, FundChatIntents intents) {
        if (intents.entries() == null || intents.vocabulary() == null
                || intents.knowledgeFirstMarkers() == null) {
            log.error("Fund chat intents file is incomplete: language={}", language);
            throw new FundChatException(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE);
        }

        if (!intents.entries().stream().map(FundChatIntentEntry::id).toList()
                .containsAll(EXPECTED_PLACEHOLDERS.keySet())) {
            log.error("Fund chat intents file does not define every known intent: language={}, expected={}",
                    language, EXPECTED_PLACEHOLDERS.keySet());
            throw new FundChatException(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE);
        }

        intents.entries().forEach(entry -> validateTemplate(language, entry));
    }

    private void validateTemplate(SupportedLanguage language, FundChatIntentEntry entry) {
        Integer expected = EXPECTED_PLACEHOLDERS.get(entry.id());

        if (expected == null) {
            log.error("Fund chat intents file defines an unknown intent: language={}, id={}",
                    language, entry.id());
            throw new FundChatException(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE);
        }

        int actual = placeholderCount(entry.template());

        if (actual != expected) {
            log.error("Fund chat template placeholder count does not match: language={}, id={}, expected={}, actual={}",
                    language, entry.id(), expected, actual);
            throw new FundChatException(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE);
        }
    }

    private int placeholderCount(String template) {
        if (template == null) {
            return -1;
        }

        return template.replace(ESCAPED_PERCENT, "").split(PLACEHOLDER, -1).length - 1;
    }

    private <T> T readJson(SupportedLanguage language, String fileName, Class<T> type) {
        try (InputStream stream = resource(language, fileName).getInputStream()) {
            return Objects.requireNonNull(objectMapper.readValue(stream, type));
        } catch (IOException | NullPointerException exception) {
            log.error("Fund chat knowledge file could not be read: language={}, file={}",
                    language, fileName, exception);
            throw new FundChatException(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, exception);
        }
    }

    private ClassPathResource resource(SupportedLanguage language, String fileName) {
        return new ClassPathResource(
                "%s/%s/%s".formatted(properties.getKnowledgePath(), language.getCode(), fileName));
    }
}
