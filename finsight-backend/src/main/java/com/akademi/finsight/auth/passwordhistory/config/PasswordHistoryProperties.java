package com.akademi.finsight.auth.passwordhistory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.password-history")
public class PasswordHistoryProperties {

    /** Kac onceki sifrenin tekrar kullanilamayacagi. */
    private int size;
}
