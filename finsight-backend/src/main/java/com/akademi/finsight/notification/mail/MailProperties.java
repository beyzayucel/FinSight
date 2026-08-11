package com.akademi.finsight.notification.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.mail")
public record MailProperties(String from) {
}
