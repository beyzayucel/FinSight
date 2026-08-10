package com.akademi.finsight.news.service.impl;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.news.config.NewsProperties;
import com.akademi.finsight.news.dto.client.NewsApiResponse;
import com.akademi.finsight.news.dto.client.NewsItem;
import com.akademi.finsight.news.dto.response.NewsResponse;
import com.akademi.finsight.news.exception.NewsApiExceededException;
import com.akademi.finsight.news.exception.NewsFetchException;
import com.akademi.finsight.news.mapper.NewsMapper;
import com.akademi.finsight.news.service.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsServiceImpl Tests")
class NewsServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private TranslationService translationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NewsMapper newsMapper;

    @Mock
    private RestClient restClient;

    @Mock
    private NewsProperties newsProperties;

    // RestClient fluent API mock'ları
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private NewsServiceImpl newsService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Nested
    @DisplayName("getNews")
    class GetNews {

        @BeforeEach
        void setUp() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            NewsProperties.Cache cache = new NewsProperties.Cache();
            cache.setKeyPrefix("news:latest:");
            lenient().when(newsProperties.getCache()).thenReturn(cache);
        }

        @Test
        @DisplayName("should return mapped news response list when cached news exists in Redis")
        void shouldReturnNewsWhenCacheHit() {
            String json = "[{\"title\":\"Borsa Yükseldi\",\"url\":\"https://example.com/1\",\"publish_date\":\"2026-08-10 06:00:00\"}]";
            List<NewsItem> items = List.of(new NewsItem("Borsa Yükseldi", "https://example.com/1", "2026-08-10 06:00:00"));
            List<NewsResponse> expectedResponses = List.of(new NewsResponse("Borsa Yükseldi", "https://example.com/1", 1L));

            when(valueOperations.get("news:latest:tr")).thenReturn(json);
            when(objectMapper.readValue(eq(json), any(TypeReference.class))).thenReturn(items);
            when(newsMapper.toResponseList(items)).thenReturn(expectedResponses);

            List<NewsResponse> actual = newsService.getNews(Locale.forLanguageTag("tr"));

            assertNotNull(actual);
            assertEquals(1, actual.size());
            assertEquals("Borsa Yükseldi", actual.get(0).title());
            verify(valueOperations).get("news:latest:tr");
            verify(newsMapper).toResponseList(items);
        }

        @Test
        @DisplayName("should return empty list when Redis cache is empty (null)")
        void shouldReturnEmptyListWhenCacheMiss() {
            when(valueOperations.get("news:latest:tr")).thenReturn(null);
            when(newsMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

            List<NewsResponse> actual = newsService.getNews(Locale.forLanguageTag("tr"));

            assertNotNull(actual);
            assertTrue(actual.isEmpty());
            verify(newsMapper).toResponseList(Collections.emptyList());
            verifyNoInteractions(objectMapper);
        }

        @Test
        @DisplayName("should default to TR language when locale is null")
        void shouldDefaultToTurkishWhenLocaleNull() {
            when(valueOperations.get("news:latest:tr")).thenReturn(null);
            when(newsMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

            List<NewsResponse> actual = newsService.getNews(null);

            assertNotNull(actual);
            assertTrue(actual.isEmpty());
            verify(valueOperations).get("news:latest:tr");
        }

        @Test
        @DisplayName("should use EN key prefix when locale is English")
        void shouldFetchEnglishNewsWhenLocaleIsEnglish() {
            when(valueOperations.get("news:latest:en")).thenReturn(null);
            when(newsMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

            List<NewsResponse> actual = newsService.getNews(Locale.ENGLISH);

            assertNotNull(actual);
            assertTrue(actual.isEmpty());
            verify(valueOperations).get("news:latest:en");
        }

        @Test
        @DisplayName("should catch exception and return empty list when Redis throws an error")
        void shouldReturnEmptyListWhenRedisThrowsException() {
            when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis connection refused"));
            when(newsMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

            List<NewsResponse> actual = newsService.getNews(Locale.forLanguageTag("tr"));

            assertNotNull(actual);
            assertTrue(actual.isEmpty());
            verify(newsMapper).toResponseList(Collections.emptyList());
        }
    }

    @Nested
    @DisplayName("fetchAndCacheNews")
    class FetchAndCacheNews {

        private void setupNewsProperties() {
            NewsProperties.Api api = new NewsProperties.Api();
            api.setUrl("https://api.worldnewsapi.com/search-news");
            api.setToken("dummy-token");
            api.setCacheSize(10);
            api.setMaxNewsAgeHours(24);

            NewsProperties.Cache cache = new NewsProperties.Cache();
            cache.setKeyPrefix("news:latest:");
            cache.setTtl(Duration.ofHours(1));

            lenient().when(newsProperties.getApi()).thenReturn(api);
            lenient().when(newsProperties.getCache()).thenReturn(cache);
        }

        private void setupRestClientMock(NewsApiResponse response) {
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(NewsApiResponse.class)).thenReturn(response);
        }

        @Test
        @DisplayName("should fetch news, filter by date and non-blank title, translate, and cache in Redis")
        void shouldFetchFilterTranslateAndCacheSuccessfully() {
            setupNewsProperties();
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            String recentDate = LocalDateTime.now().minusHours(2).format(FORMATTER);
            String oldDate = LocalDateTime.now().minusHours(30).format(FORMATTER); // > 24 hours ago

            NewsItem validItem = new NewsItem("Faiz Kararı Açıklandı", "https://example.com/valid", recentDate);
            NewsItem blankTitleItem = new NewsItem("   ", "https://example.com/blank", recentDate);
            NewsItem oldItem = new NewsItem("Eski Haber", "https://example.com/old", oldDate);
            NewsItem nullDateItem = new NewsItem("Tarihsiz Haber", "https://example.com/nodate", null);

            NewsApiResponse apiResponse = new NewsApiResponse(0, 4, 4, List.of(validItem, blankTitleItem, oldItem, nullDateItem));
            setupRestClientMock(apiResponse);

            NewsItem translatedItem = new NewsItem("Interest Rate Decision Announced", "https://example.com/valid", recentDate);
            when(translationService.translate(List.of(validItem), SupportedLanguage.EN)).thenReturn(List.of(translatedItem));
            when(objectMapper.writeValueAsString(List.of(validItem))).thenReturn("[\"tr_json\"]");
            when(objectMapper.writeValueAsString(List.of(translatedItem))).thenReturn("[\"en_json\"]");

            assertDoesNotThrow(() -> newsService.fetchAndCacheNews());

            // TR ve EN olarak Redis'e kaydedilmeli
            verify(valueOperations).set("news:latest:tr", "[\"tr_json\"]", Duration.ofHours(1));
            verify(valueOperations).set("news:latest:en", "[\"en_json\"]", Duration.ofHours(1));
            verify(translationService).translate(List.of(validItem), SupportedLanguage.EN);
        }

        @Test
        @DisplayName("should log warning and not cache anything when API returns empty news list")
        void shouldDoNothingWhenApiResponseIsEmpty() {
            setupNewsProperties();
            NewsApiResponse emptyResponse = new NewsApiResponse(0, 0, 0, Collections.emptyList());
            setupRestClientMock(emptyResponse);

            assertDoesNotThrow(() -> newsService.fetchAndCacheNews());

            verifyNoInteractions(translationService);
            verifyNoInteractions(redisTemplate);
        }

        @Test
        @DisplayName("should log warning and not cache anything when API returns null response")
        void shouldDoNothingWhenApiResponseIsNull() {
            setupNewsProperties();
            setupRestClientMock(null);

            assertDoesNotThrow(() -> newsService.fetchAndCacheNews());

            verifyNoInteractions(translationService);
            verifyNoInteractions(redisTemplate);
        }

        @Test
        @DisplayName("should throw NewsApiExceededException when API returns HTTP 429 Too Many Requests")
        void shouldThrowNewsApiExceededExceptionOnRateLimit() {
            setupNewsProperties();
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

            HttpClientErrorException rateLimitException =
                    HttpClientErrorException.create(
                            HttpStatus.TOO_MANY_REQUESTS,
                            "Too Many Requests",
                            HttpHeaders.EMPTY,
                            new byte[0],
                            StandardCharsets.UTF_8
                    );
            when(responseSpec.body(NewsApiResponse.class)).thenThrow(rateLimitException);

            assertThrows(NewsApiExceededException.class, () -> newsService.fetchAndCacheNews());
            verifyNoInteractions(translationService);
            verifyNoInteractions(redisTemplate);
        }

        @Test
        @DisplayName("should throw NewsFetchException when unexpected error occurs during fetch")
        void shouldThrowNewsFetchExceptionOnGenericError() {
            setupNewsProperties();
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(NewsApiResponse.class)).thenThrow(new RuntimeException("Connection timeout"));

            assertThrows(NewsFetchException.class, () -> newsService.fetchAndCacheNews());
            verifyNoInteractions(translationService);
            verifyNoInteractions(redisTemplate);
        }
    }
}
