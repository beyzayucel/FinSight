package com.akademi.finsight.stresstest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "stress-test.onnx")
public class OnnxProperties {
    private int intraOpNumThreads;
    private int interOpNumThreads;
}
