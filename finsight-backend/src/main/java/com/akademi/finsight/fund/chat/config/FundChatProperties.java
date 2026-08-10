package com.akademi.finsight.fund.chat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fund-chat")
public class FundChatProperties {

    private Duration sessionTtl = Duration.ofHours(1);

    private int maxTurns = 10;

    private String knowledgePath = "fund-chat";
}
