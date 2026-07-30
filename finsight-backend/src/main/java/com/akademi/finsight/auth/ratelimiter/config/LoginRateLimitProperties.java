package com.akademi.finsight.auth.ratelimiter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit.login")
public class LoginRateLimitProperties {
    private int maxAttempts;
    private Duration duration;
    private Duration blockDuration;
}
