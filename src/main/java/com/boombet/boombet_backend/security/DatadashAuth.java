package com.boombet.boombet_backend.security;

import com.boombet.boombet_backend.dto.DatadashDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DatadashAuth {

    private final RestClient restClient;

    @Value("${datadash.auth.email}")
    private String email;

    @Value("${datadash.auth.password}")
    private String password;


    public DatadashAuth(@Qualifier("datadashRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Cacheable(value = "datadashToken", unless = "#result == null")
    public String getAccessToken() {


        var request = new DatadashDTO.DatadashLogin(email, password);

        // Hacemos la petición de Login
        DatadashDTO.AuthResponseWrapper response = this.restClient.post()
                .uri("/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(DatadashDTO.AuthResponseWrapper.class);


        return response != null && response.token() != null ? response.token().accessToken() : null;
    }
}