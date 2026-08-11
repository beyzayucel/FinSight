package com.akademi.finsight.news.service.impl;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.news.dto.client.NewsItem;
import com.akademi.finsight.news.service.DeepLService;
import com.akademi.finsight.news.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

    private final DeepLService deepLService;

    public List<NewsItem> translate(List<NewsItem> news, SupportedLanguage language) {
        return news.stream()
                .map(item -> {
                    String translatedTitle = item.title();
                    if (Objects.nonNull(item.title()) && !item.title().isBlank()) {
                        try {
                            translatedTitle = deepLService.translate(item.title(), language);
                        } catch (Exception e) {
                            log.warn("DeepL translation failed for title: '{}'. Falling back to original title. Error: {}", 
                                    item.title(), e.getMessage());
                        }
                    }
                    return new NewsItem(translatedTitle, item.url(), item.publishDate());
                })
                .toList();
    }
}
