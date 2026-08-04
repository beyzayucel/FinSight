package com.akademi.finsight.integration.infina.config;

import com.akademi.finsight.integration.infina.client.InfinaServicesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class InfinaClientConfig {

	@Bean
	public HttpServiceProxyFactory httpServiceProxyFactory(
			@Value("${infina.api.base-url}") String baseUrl,
			@Value("${infina.api.key}") String apiKey) {

		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(3))
				.build();

		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(Duration.ofSeconds(10));

		RestClient restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader("X-API-Key", apiKey)
				.requestFactory(requestFactory)
				.build();

		return HttpServiceProxyFactory
				.builderFor(RestClientAdapter.create(restClient))
				.build();
	}

	@Bean
	public InfinaServicesClient servicesRestClient(HttpServiceProxyFactory factory){
		return factory.createClient(InfinaServicesClient.class);
	}
}
