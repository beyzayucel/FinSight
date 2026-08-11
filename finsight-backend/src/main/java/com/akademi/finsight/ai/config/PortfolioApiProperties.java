package com.akademi.finsight.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "portfolio.api")
public record PortfolioApiProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
}
