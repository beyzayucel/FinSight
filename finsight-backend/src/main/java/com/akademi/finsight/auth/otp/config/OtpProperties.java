package com.akademi.finsight.auth.otp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.otp")
public class OtpProperties {
    private Duration expireDuration;
    private Duration cooldownDuration;
    private int maxAttempts;
    private final Abuse abuse = new Abuse();

    public long getCooldownSeconds() {
        return cooldownDuration != null ? cooldownDuration.getSeconds() : 60L;
    }

    @Getter
    @Setter
    public static class Abuse {
        private int maxCycles;
        private Duration blockDuration;
        private Duration windowDuration;
    }
}
