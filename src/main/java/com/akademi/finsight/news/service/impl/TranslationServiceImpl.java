package com.akademi.finsight.news.service.impl;

import com.akademi.finsight.news.dto.client.NewsItem;
import com.akademi.finsight.news.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

    private final DeepLServiceImpl deepLService;

    public List<NewsItem> translateToEnglish(List<NewsItem> news) {
        return news.stream()
                .map(item -> {
                    String translatedTitle = (Objects.nonNull(item.title()) && !item.title().isBlank())
                            ? deepLService.translateToEnglish(item.title())
                            : item.title();
                    return new NewsItem(translatedTitle, item.url(), item.publishDate());
                })
                .toList();
    }
}
