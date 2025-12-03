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
            @Value("${affiliator.api.key}")String apiKey) {

        RestClient clientBase = buildClient(baseUrl, Duration.ofMinutes(5));
        return clientBase.mutate()
                .defaultHeader("register_key", apiKey)
                .build();
    }

    @Bean("bondaAffiliatesClient")
    public RestClient bondaAffiliatesClient(
            @Value("${bonda.api.base-url}") String baseUrl,
            @Value("${bonda.api.key}") String token) {

        return buildClient(baseUrl, Duration.ofSeconds(30)).mutate()
                // IMPORTANTE: Solo enviamos el header 'token'.
                // No enviamos 'microsite_id' aquí porque va en la URL.
                .defaultHeader("token", token)
                .build();
    }

    @Bean("bondaCouponsClient")
    public RestClient bondaCouponsClient(
            @Value("${bonda.api.base-url}") String baseUrl) {

        // Este cliente va "limpio" de headers de seguridad porque
        // se los pasaremos manualemente en la URL (Query Params)
        return buildClient(baseUrl, Duration.ofSeconds(30));
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