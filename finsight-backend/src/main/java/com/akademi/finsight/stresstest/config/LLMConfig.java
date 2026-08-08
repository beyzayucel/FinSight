package com.akademi.finsight.stresstest.config;

import com.akademi.finsight.stresstest.llm.LLMCommentGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LLMConfig {

    @Bean
    @Primary
    public LLMCommentGenerator activeLlmCommentGenerator(
            @Qualifier("templateLlmGenerator") LLMCommentGenerator templateGenerator) {
        return templateGenerator;
    }
}