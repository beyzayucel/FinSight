package com.akademi.finsight.stresstest.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaAuditingFixConfig {

    /**
     * AuditingEntityListener'ın prototype yapısını bozmadan (güncel tarih ve kullanıcı verilerinin
     * kaybolmasını engelleyerek), jpaAuditingHandler'ın jpaMappingContext'e bağımlı olduğunu
     * Spring IoC konteynerine bildirir.
     */
    @Bean
    public static BeanFactoryPostProcessor jpaAuditingDependencyPostProcessor() {
        return beanFactory -> {
            if (beanFactory.containsBeanDefinition("jpaAuditingHandler")) {
                beanFactory.getBeanDefinition("jpaAuditingHandler")
                        .setDependsOn("jpaMappingContext");
            }
        };
    }
}
