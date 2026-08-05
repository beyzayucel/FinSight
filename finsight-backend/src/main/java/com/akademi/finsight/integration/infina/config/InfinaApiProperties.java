package com.akademi.finsight.integration.infina.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "infina.api")
public class InfinaApiProperties {

    private String baseUrl;
    private String key;
    private Duration connectTimeout;
    private Duration readTimeout;
}
