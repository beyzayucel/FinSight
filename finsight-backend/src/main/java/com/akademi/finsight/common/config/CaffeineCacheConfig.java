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
        FundProperties.Cache.PerformanceComparison performanceComparison =
                fundProperties.getCache().getPerformanceComparison();
        FundProperties.Cache.DecisionHistory decisionHistory =
                fundProperties.getCache().getDecisionHistory();

        CaffeineCacheManager manager = new CaffeineCacheManager(CacheNames.PERFORMANCE_COMPARISON);
        manager.setCaffeine(buildCache(performanceComparison.getMaxSize(), performanceComparison.getExpireHours()));
        manager.registerCustomCache(
                CacheNames.DECISION_HISTORY,
                buildCache(decisionHistory.getMaxSize(), decisionHistory.getExpireHours()).build());
        return manager;
    }

    private Caffeine<Object, Object> buildCache(int maxSize, int expireHours) {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(Duration.ofHours(expireHours))
                .recordStats();
    }
}
