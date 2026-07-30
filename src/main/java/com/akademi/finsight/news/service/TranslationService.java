package com.akademi.finsight.news.service;

import com.akademi.finsight.news.dto.client.NewsItem;

import java.util.List;

public interface TranslationService {
    List<NewsItem> translateToEnglish(List<NewsItem> news);
}
