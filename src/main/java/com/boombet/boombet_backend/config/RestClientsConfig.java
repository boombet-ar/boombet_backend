package com.boombet.boombet_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientsConfig {

    @Bean("datadashRestClient")
    public RestClient datadashRestClient(
            @Value("${datadash.api.base-url}") String baseUrl) {
        return buildClient(baseUrl, Duration.ofSeconds(10));
    }

    @Bean("affiliatorRestClient")
    public RestClient affiliatorRestClient(
            @Value("${affiliator.api.base-url}") String baseUrl,
            @Value("$affiliator.api.key")String apiKey) {

        RestClient clientBase = buildClient(baseUrl, Duration.ofMinutes(5));
        return clientBase.mutate()
                .defaultHeader("register_key", apiKey)
                .build();
    }

    @Bean("bondaRestClient")
    public RestClient bondaRestClient(
            @Value("${bonda.api.base-url}") String baseUrl,
            @Value("${bonda.api.key}") String token,
            @Value("${bonda.microsite.id}") String siteId) {

        RestClient clientBase = buildClient(baseUrl, Duration.ofSeconds(30));

        return clientBase.mutate()
                .defaultHeader("microsite_id", siteId)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    private RestClient buildClient(String baseUrl, Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(30));

        factory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl(baseUrl)
                .build();
    }
}