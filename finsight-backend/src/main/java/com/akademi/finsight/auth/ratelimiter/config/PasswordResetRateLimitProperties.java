package com.akademi.finsight.auth.ratelimiter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Sifre sifirlama talebi icin istek hizi siniri. Login limitinden ayridir:
 * orada "basarisiz deneme" sayilir, burada talebin kendisi sayilir.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit.password-reset")
public class PasswordResetRateLimitProperties {
    private int maxRequestsPerEmail;
    private Duration duration;
    private Duration cooldownDuration;
}
