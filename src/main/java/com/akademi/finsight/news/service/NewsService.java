package com.akademi.finsight.news.service;

import com.akademi.finsight.news.dto.response.NewsResponse;

import java.util.List;
import java.util.Locale;

public interface NewsService {
    List<NewsResponse> getNews(Locale locale);
    void fetchAndCacheNews();

}
