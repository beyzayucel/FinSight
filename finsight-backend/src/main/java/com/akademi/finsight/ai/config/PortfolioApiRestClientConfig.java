package com.akademi.finsight.ai.config;

import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PortfolioApiRestClientConfig {

    @Bean
    public RestClient portfolioApiRestClient(PortfolioApiProperties properties,
                                             ObjectMapper portfolioApiObjectMapper) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }
}