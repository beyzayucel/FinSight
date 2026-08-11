package com.akademi.finsight.integration.infina.config;

import com.akademi.finsight.integration.infina.client.InfinaServicesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;

@Configuration
@RequiredArgsConstructor
public class InfinaClientConfig {

	private final InfinaApiProperties properties;

	@Bean
	public HttpServiceProxyFactory httpServiceProxyFactory() {

		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(properties.getConnectTimeout())
				.build();

		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(properties.getReadTimeout());

		RestClient restClient = RestClient.builder()
				.baseUrl(properties.getBaseUrl())
				.defaultHeader("X-API-Key", properties.getKey())
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
