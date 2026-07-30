package com.akademi.finsight.news.service.impl;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.news.dto.client.NewsItem;
import com.akademi.finsight.news.service.DeepLService;
import com.akademi.finsight.news.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

    private final DeepLService deepLService;

    public List<NewsItem> translate(List<NewsItem> news, SupportedLanguage language) {
        return news.stream()
                .map(item -> {
                    String translatedTitle = (Objects.nonNull(item.title()) && !item.title().isBlank())
                            ? deepLService.translate(item.title(), language)
                            : item.title();
                    return new NewsItem(translatedTitle, item.url(), item.publishDate());
                })
                .toList();
    }
}
