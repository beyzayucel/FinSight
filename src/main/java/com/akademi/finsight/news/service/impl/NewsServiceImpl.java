package com.akademi.finsight.news.service.impl;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.news.dto.client.NewsApiResponse;
import com.akademi.finsight.news.dto.client.NewsItem;
import com.akademi.finsight.news.dto.response.NewsResponse;
import com.akademi.finsight.news.mapper.NewsMapper;
import com.akademi.finsight.news.service.NewsService;
import com.akademi.finsight.news.service.TranslationService;
import com.akademi.finsight.news.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import java.net.URI;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsServiceImpl implements NewsService {
    private static final String REDIS_NEWS_KEY = "infina:latest_news:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;
    private final TranslationService translationService;
    private final ObjectMapper objectMapper;
    private final NewsMapper newsMapper;
    private final RestClient restClient;

    @Value("${news.api.url}")
    private String apiUrl;

    @Value("${news.api.token}")
    private String apiToken;

    @Value("${news.api.search-text}")
    private String searchText;

    @Value("${news.api.cache-size}")
    private int newsCacheSize;

    @Override
    public List<NewsResponse> getNews(Locale locale) {
        List<NewsItem> news = getCachedNews(locale);
        if (news.isEmpty()) {
            fetchAndSaveNews();
            news = getCachedNews(locale);
            if (news.isEmpty()) {
                log.info("No recent news found.");
            }
        }
        return newsMapper.toResponseList(news);
    }

    @Override
    public void fetchAndSaveNews() {
        try {
            URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("language", "tr")
                    .queryParam("api-key", apiToken)
                    .queryParam("text", searchText)
                    .queryParam("sort", "publish-time")
                    .queryParam("sort-direction", "DESC")
                    .build()
                    .toUri();

            log.debug("Requesting News API URL: {}", uri);

            NewsApiResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(NewsApiResponse.class);

            if (Objects.isNull(response) || Objects.isNull(response.news()) || response.news().isEmpty()) {
                log.warn("News API returned an empty response.");
                return;
            }
            List<NewsItem> latestNews = getLatestNews(response.news());
            List<NewsItem> englishNews = translationService.translateToEnglish(latestNews);

            redisTemplate.opsForValue().set(
                    REDIS_NEWS_KEY + SupportedLanguage.TR.getCode(),
                    objectMapper.writeValueAsString(latestNews),
                    CACHE_TTL);

            redisTemplate.opsForValue().set(
                    REDIS_NEWS_KEY + SupportedLanguage.EN.getCode(),
                    objectMapper.writeValueAsString(englishNews),
                    CACHE_TTL);

            log.info("Cached {} news items in Redis for locales: tr, en.", latestNews.size());

        } catch (Exception e) {
            log.error("Error while fetching news", e);
        }
    }

    private List<NewsItem> getLatestNews(List<NewsItem> rawNews) {
        return rawNews.stream()
                .filter(item -> Objects.nonNull(item.title()) && !item.title().isBlank())
                .filter(item -> Objects.nonNull(item.publishDate()))
                .filter(item -> {
                    Long hoursAgo = DateUtil.getHoursAgo(item.publishDate());
                    return hoursAgo != null && hoursAgo < 15;
                })
                .sorted(Comparator.comparing(NewsItem::publishDate).reversed())
                .limit(newsCacheSize)
                .toList();
    }

    private List<NewsItem> getCachedNews(Locale locale) {
        try {
            String key = REDIS_NEWS_KEY + locale.getLanguage();
            String json = redisTemplate.opsForValue().get(key);
            if (Objects.isNull(json)) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Redis read error", e);
            return Collections.emptyList();
        }
    }
}