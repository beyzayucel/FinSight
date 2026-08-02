package com.akademi.finsight.otp.config;

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

    public long getCooldownSeconds() {
        return cooldownDuration != null ? cooldownDuration.getSeconds() : 60L;
    }

}
