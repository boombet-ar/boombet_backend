package com.boombet.boombet_backend.restClients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class DatadashClient {



         @Value("${datadash.api.base-url}")
         private String baseUrl;



        @Bean("datadashRestClient")
        public RestClient dataDashClient(RestClient.Builder builder) {

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofSeconds(30));
            factory.setReadTimeout(Duration.ofSeconds(10));

            return builder
                    .requestFactory(factory)
                    .baseUrl(baseUrl)
                    .build();
        }

}
