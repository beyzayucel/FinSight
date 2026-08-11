package com.akademi.finsight.auth.ratelimiter.filter;

import com.akademi.finsight.common.constants.ApiEndpoints;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class CachedBodyFilterConfig {

    @Bean
    public FilterRegistrationBean<CachedBodyFilter> cachedBodyFilter(){
        FilterRegistrationBean<CachedBodyFilter> bean =
                new FilterRegistrationBean<>();

        bean.setFilter(new CachedBodyFilter());

        // Rate limit interceptor'lari istek govdesini okuyor; govde once burada cache'lenmeli
        bean.addUrlPatterns(
                ApiEndpoints.Auth.BASE + ApiEndpoints.Auth.LOGIN,
                ApiEndpoints.Auth.BASE + ApiEndpoints.Auth.FORGOT_PASSWORD
        );

        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return bean;
    }
}
