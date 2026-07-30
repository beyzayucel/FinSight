package com.akademi.finsight.news.service;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.news.dto.client.NewsItem;

import java.util.List;

public interface TranslationService {
    List<NewsItem> translate(List<NewsItem> news, SupportedLanguage language);
}
