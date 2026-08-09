package com.akademi.finsight.common.config;

import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.fund.constant.CacheNames;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CaffeineCacheConfig {

    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager(FundProperties fundProperties) {
        FundProperties.Cache.PerformanceComparison settings =
                fundProperties.getCache().getPerformanceComparison();

        CaffeineCacheManager manager = new CaffeineCacheManager(CacheNames.PERFORMANCE_COMPARISON);
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(settings.getMaxSize())
                .expireAfterWrite(Duration.ofHours(settings.getExpireHours()))
                .recordStats());
        return manager;
    }
}
