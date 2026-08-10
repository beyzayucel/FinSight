package com.akademi.finsight.fund.chat.service.impl;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.fund.chat.context.FundChatContextBuilder;
import com.akademi.finsight.fund.chat.dto.FundChatContext;
import com.akademi.finsight.fund.chat.dto.FundChatPrompt;
import com.akademi.finsight.fund.chat.dto.FundChatReply;
import com.akademi.finsight.fund.chat.dto.FundChatTurn;
import com.akademi.finsight.fund.chat.dto.request.FundChatRequest;
import com.akademi.finsight.fund.chat.dto.response.FundChatResponse;
import com.akademi.finsight.fund.chat.exception.FundChatErrorType;
import com.akademi.finsight.fund.chat.exception.FundChatException;
import com.akademi.finsight.fund.chat.knowledge.FundChatContent;
import com.akademi.finsight.fund.chat.knowledge.FundChatKnowledgeBase;
import com.akademi.finsight.fund.chat.memory.FundChatMemoryStore;
import com.akademi.finsight.fund.chat.provider.FundChatProvider;
import com.akademi.finsight.fund.chat.service.FundChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundChatServiceImpl implements FundChatService {

    private final FundChatProvider fundChatProvider;
    private final FundChatKnowledgeBase knowledgeBase;
    private final FundChatContextBuilder contextBuilder;
    private final FundChatMemoryStore memoryStore;

    @Override
    public FundChatResponse ask(String email, String fundCode, FundChatRequest request) {
        String sessionId = request.sessionId() == null || request.sessionId().isBlank()
                ? UUID.randomUUID().toString()
                : request.sessionId();

        SupportedLanguage language = resolveLanguage();
        FundChatContent content = knowledgeBase.content(language);

        List<FundChatTurn> history = memoryStore.load(email, fundCode, sessionId);
        FundChatContext context = contextBuilder.build(fundCode);

        FundChatPrompt prompt =
                new FundChatPrompt(fundCode, language, content, context, history, request.message());

        FundChatReply reply = generate(prompt, fundCode, email);
        Instant answeredAt = Instant.now();

        List<FundChatTurn> updated = new ArrayList<>(history);
        updated.add(FundChatTurn.user(request.message(), answeredAt));
        updated.add(FundChatTurn.assistant(reply.text(), answeredAt));
        memoryStore.save(email, fundCode, sessionId, updated);

        log.info("Fund chat answered: fundCode={}, user={}, sessionId={}, language={}, source={}, historyTurns={}",
                fundCode, MaskType.EMAIL.mask(email), sessionId, language, reply.source(), history.size());

        return new FundChatResponse(sessionId, reply.text(), reply.source(), answeredAt);
    }

    @Override
    public void reset(String email, String fundCode, String sessionId) {
        memoryStore.clear(email, fundCode, sessionId);
        log.info("Fund chat session cleared: fundCode={}, user={}, sessionId={}",
                fundCode, MaskType.EMAIL.mask(email), sessionId);
    }

    private SupportedLanguage resolveLanguage() {
        String requested = LocaleContextHolder.getLocale().getLanguage();
        return SupportedLanguage.TR.getCode().equals(requested)
                ? SupportedLanguage.TR
                : SupportedLanguage.EN;
    }

    private FundChatReply generate(FundChatPrompt prompt, String fundCode, String email) {
        try {
            return fundChatProvider.generate(prompt);
        } catch (FundChatException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("Fund chat provider failed: event=FUND_CHAT_FAILED, fundCode={}, user={}",
                    fundCode, MaskType.EMAIL.mask(email), exception);
            throw new FundChatException(FundChatErrorType.FUND_CHAT_PROVIDER_FAILED, exception);
        }
    }
}
