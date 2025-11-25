package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dto.DatadashDTO;
import com.boombet.boombet_backend.security.DatadashAuth;
import com.boombet.boombet_backend.utils.UsuarioUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DatadashService {


    private final DatadashAuth datadashAuth;
    private final RestClient restClient;


    public DatadashService(DatadashAuth datadashAuth,
                           @Qualifier("datadashRestClient") RestClient restClient
                           ) {
        this.datadashAuth = datadashAuth;
        this.restClient = restClient;
    }


    public DatadashDTO.DatadashInformResponse getUserData(String dni, Character genero) {

        String cuil = UsuarioUtils.generarCuit(dni, genero);
        DatadashDTO.DatadashInformRequest requestBody = new DatadashDTO.DatadashInformRequest(cuil);

        String token = datadashAuth.getAccessToken();

        if (token == null) {
            throw new RuntimeException("No se pudo obtener el token de acceso para Datadash");
        }

        return restClient.post()
                .uri("/informes/informe")
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(DatadashDTO.DatadashInformResponse.class);
    }
}