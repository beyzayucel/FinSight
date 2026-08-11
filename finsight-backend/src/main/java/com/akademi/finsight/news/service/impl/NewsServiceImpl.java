package com.akademi.finsight.news.service.impl;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.news.config.NewsProperties;
import com.akademi.finsight.news.dto.client.NewsApiResponse;
import com.akademi.finsight.news.dto.client.NewsItem;
import com.akademi.finsight.news.dto.response.NewsResponse;
import com.akademi.finsight.news.exception.NewsApiExceededException;
import com.akademi.finsight.news.exception.NewsFetchException;
import com.akademi.finsight.news.mapper.NewsMapper;
import com.akademi.finsight.news.service.NewsService;
import com.akademi.finsight.news.service.TranslationService;
import com.akademi.finsight.news.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsServiceImpl implements NewsService {

    private static final String NEWS_SEARCH_QUERY = "\"hisse senedi\" OR TCMB OR \"faiz kararı\" OR borsa OR döviz OR \"halka arz\" OR tahvil OR finans";
    private static final String NEWS_CATEGORIES = "business";

    private final StringRedisTemplate redisTemplate;
    private final TranslationService translationService;
    private final ObjectMapper objectMapper;
    private final NewsMapper newsMapper;
    private final RestClient restClient;
    private final NewsProperties newsProperties;

    @Override
    public List<NewsResponse> getNews(Locale locale) {
        List<NewsItem> news = getCachedNews(locale);
        return newsMapper.toResponseList(news);
    }

    @Override
    public void fetchAndCacheNews() {
        try {
            URI uri = buildNewsApiUri();
            NewsApiResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(NewsApiResponse.class);

            if (Objects.isNull(response) || Objects.isNull(response.news()) || response.news().isEmpty()) {
                log.warn("News API returned an empty response.");
                return;
            }
            List<NewsItem> latestNews = getLatestNews(response.news());
            List<NewsItem> englishNews = translationService.translate(latestNews, SupportedLanguage.EN);
            cacheNews(latestNews, SupportedLanguage.TR);
            cacheNews(englishNews, SupportedLanguage.EN);

            log.info("Cached {} news items in Redis for locales: tr, en.", latestNews.size());

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("API Quota exceeded (HTTP 429 Too Many Requests).", e);
            throw new NewsApiExceededException(e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching news", e);
            throw new NewsFetchException(e);
        }
    }

    private List<NewsItem> getLatestNews(List<NewsItem> rawNews) {
        return rawNews.stream()
                .filter(item -> Objects.nonNull(item.title()) && !item.title().isBlank())
                .filter(item -> Objects.nonNull(item.publishDate()))
                .filter(item -> {
                    Long hoursAgo = DateUtil.getHoursAgo(item.publishDate());
                    return hoursAgo != null && hoursAgo < newsProperties.getApi().getMaxNewsAgeHours();
                })
                .sorted(Comparator.comparing(NewsItem::publishDate).reversed())
                .limit(newsProperties.getApi().getCacheSize())
                .toList();
    }

    private List<NewsItem> getCachedNews(Locale locale) {
        try {
            String language = locale != null ? locale.getLanguage() : SupportedLanguage.TR.getCode();

            String key = newsProperties.getCache().getKeyPrefix() + language;
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

    private URI buildNewsApiUri() {
        return UriComponentsBuilder.fromUriString(newsProperties.getApi().getUrl())
                .queryParam("language", "tr")
                .queryParam("api-key", newsProperties.getApi().getToken())
                .queryParam("text", NEWS_SEARCH_QUERY)
                .queryParam("categories", NEWS_CATEGORIES)
                .queryParam("sort", "publish-time")
                .queryParam("sort-direction", "DESC")
                .encode(java.nio.charset.StandardCharsets.UTF_8)
                .build()
                .toUri();
    }

    private void cacheNews(List<NewsItem> news, SupportedLanguage language) {
        redisTemplate.opsForValue().set(
                newsProperties.getCache().getKeyPrefix() + language.getCode(),
                objectMapper.writeValueAsString(news),
                newsProperties.getCache().getTtl());
    }
}