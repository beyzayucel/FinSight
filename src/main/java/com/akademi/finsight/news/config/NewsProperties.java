package com.akademi.finsight.news.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "news")
public class NewsProperties {

    private final Api api = new Api();
    private final Cache cache = new Cache();

    @Getter
    @Setter
    public static class Api {
        private String url;
        private String token;
        private String searchText;
        private int cacheSize;
        private int maxNewsAgeHours;
    }

    @Getter
    @Setter
    public static class Cache {
        private String keyPrefix;
        private Duration ttl;
    }
}