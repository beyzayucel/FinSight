package com.akademi.finsight.common.config;


import com.akademi.finsight.common.constants.ApiEndpoints;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;


@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";
    private static final String API_TITLE = "Infina Auth Service API";
    private static final String API_VERSION = "1.0";
    private static final String BEARER_SCHEME = "bearer";
    private static final String BEARER_FORMAT = "JWT";

    private static final Set<String> PUBLIC_PATHS = Set.of(
            ApiEndpoints.Auth.BASE + ApiEndpoints.Auth.LOGIN,
            ApiEndpoints.Auth.BASE + ApiEndpoints.Auth.REFRESH
    );

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(API_TITLE)
                        .version(API_VERSION))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(BEARER_SCHEME)
                                .bearerFormat(BEARER_FORMAT)));
    }

    @Bean
    public OpenApiCustomizer securityCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
            if (PUBLIC_PATHS.contains(path)) {
                return;
            }
            pathItem.readOperations().forEach(operation ->
                    operation.addSecurityItem(new SecurityRequirement()
                            .addList(SECURITY_SCHEME_NAME))
            );
        });
    }
}