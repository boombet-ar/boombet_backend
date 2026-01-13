package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dto.DatadashDTO;
import com.boombet.boombet_backend.dto.UserDataRequestDTO;
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


    /**
     * Devuelve los datos del usuario extraídos de la api de datadash. Se utiliza previo al registro
     * @param input Son basicamente los datos de registro
     * @return
     */
    public DatadashDTO.DatadashInformResponse getUserData(UserDataRequestDTO input) {

        String cuil = UsuarioUtils.generarCuit(input);
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
                .onStatus(status -> status.value() == 400, (request, response) -> {
                    throw new IllegalArgumentException("El género seleccionado es incorrecto. Por favor, verificá tus datos e intentalo nuevamente.");
                })
                .body(DatadashDTO.DatadashInformResponse.class);
}}