package com.akademi.finsight.fund.chat.knowledge;

import com.akademi.finsight.fund.chat.config.FundChatProperties;
import com.akademi.finsight.fund.chat.exception.FundChatErrorType;
import com.akademi.finsight.fund.chat.exception.FundChatException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class FundChatKnowledgeBase {

    private static final String SYSTEM_PROMPT_FILE = "system-prompt.md";
    private static final String GLOSSARY_FILE = "glossary.md";
    private static final String FAQ_FILE = "faq.json";

    private final FundChatProperties properties;
    private final ObjectMapper objectMapper;

    private String systemPrompt;
    private String glossary;
    private FundChatFaq faq;

    @PostConstruct
    void load() {
        systemPrompt = readText(SYSTEM_PROMPT_FILE);
        glossary = readText(GLOSSARY_FILE);
        faq = readFaq();

        log.info("Fund chat knowledge loaded: path={}, faqEntries={}",
                properties.getKnowledgePath(), faq.entries().size());
    }

    private String readText(String fileName) {
        try (InputStream stream = resource(fileName).getInputStream()) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            log.error("Fund chat knowledge file could not be read: file={}", fileName, exception);
            throw new FundChatException(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, exception);
        }
    }

    private FundChatFaq readFaq() {
        try (InputStream stream = resource(FAQ_FILE).getInputStream()) {
            FundChatFaq loaded = objectMapper.readValue(stream, FundChatFaq.class);

            if (loaded == null || loaded.fallback() == null || loaded.entries() == null) {
                log.error("Fund chat FAQ is missing a fallback or an entry list: file={}", FAQ_FILE);
                throw new FundChatException(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE);
            }

            return loaded;
        } catch (IOException exception) {
            log.error("Fund chat FAQ could not be read: file={}", FAQ_FILE, exception);
            throw new FundChatException(FundChatErrorType.FUND_CHAT_KNOWLEDGE_UNAVAILABLE, exception);
        }
    }

    private ClassPathResource resource(String fileName) {
        return new ClassPathResource(properties.getKnowledgePath() + "/" + fileName);
    }

    public List<FundChatFaqEntry> faqEntries() {
        return faq.entries();
    }

    public String fallbackAnswer() {
        return faq.fallback();
    }
}
