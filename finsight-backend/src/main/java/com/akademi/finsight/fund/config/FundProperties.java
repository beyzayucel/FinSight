package com.akademi.finsight.fund.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fund")
public class FundProperties {

    private String code;

    private List<String> periods;
}
