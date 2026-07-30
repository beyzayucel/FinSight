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

        bean.addUrlPatterns(ApiEndpoints.Auth.BASE + ApiEndpoints.Auth.LOGIN);

        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return bean;
    }
}
