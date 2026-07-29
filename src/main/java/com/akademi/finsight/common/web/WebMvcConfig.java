package com.akademi.finsight.common.web;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.security.web.FirstLoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final FirstLoginInterceptor firstLoginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(firstLoginInterceptor)
                .addPathPatterns(ApiEndpoints.API_V1 + "/**")
                .excludePathPatterns(
                        ApiEndpoints.Auth.BASE + ApiEndpoints.Auth.LOGIN,
                        ApiEndpoints.Auth.BASE + ApiEndpoints.Auth.REFRESH,
                        ApiEndpoints.Auth.BASE + ApiEndpoints.Auth.CHANGE_PASSWORD
                );
    }
}
