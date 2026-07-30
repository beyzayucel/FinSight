package com.akademi.finsight.news.service.impl;

import com.akademi.finsight.common.constants.SupportedLanguage;
import com.akademi.finsight.news.config.DeepLProperties;
import com.akademi.finsight.news.dto.client.DeepLResponse;
import com.akademi.finsight.news.service.DeepLService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DeepLServiceImpl implements DeepLService {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_PREFIX = "DeepL-Auth-Key ";
    private static final String TEXT_KEY = "text";
    private static final String TARGET_LANGUAGE_KEY = "target_lang";

    private final RestClient restClient;
    private final DeepLProperties deepLProperties;

    @Override
    public String translate(String text, SupportedLanguage language) {

        Map<String, Object> requestBody = Map.of(
                TEXT_KEY, List.of(text),
                TARGET_LANGUAGE_KEY, language.getCode());

        DeepLResponse responseBody = restClient.post()
                .uri(deepLProperties.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, AUTHORIZATION_PREFIX + deepLProperties.getKey())
                .body(requestBody)
                .retrieve()
                .body(DeepLResponse.class);

        if (Objects.isNull(responseBody) || Objects.isNull(responseBody.translations()) || responseBody.translations().isEmpty()) {
            throw new IllegalStateException("Invalid response from DeepL API");
        }

        return responseBody.translations().getFirst().text();
    }
}
