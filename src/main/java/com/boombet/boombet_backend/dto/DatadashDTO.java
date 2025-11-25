package com.boombet.boombet_backend.dto;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class DatadashDTO {
    public record DatadashLogin(
            String email,
            String password
    ){}

    public record DatadashInformRequest(
            String cuil
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DatadashInformResponse(
            @JsonProperty("listaExistenciaFisica")
            List<DatosPersonales> datos
    ){}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DatosPersonales(


            @JsonProperty("apenom")
            String nombreCompleto,

            @JsonProperty("sexo")
            String genero,

            @JsonProperty("fecha_nacimiento")
            String fechaNacimiento,

            @JsonProperty("nume_docu")
            Long dni,
            @JsonProperty("cdi_codigo_de_identificacion")
            String cuit,
            @JsonProperty("estado_civil")
            String estadoCivil,
            @JsonProperty("direc_calle")
            String direccion,
            @JsonProperty("localidad")
            String localidad,
            @JsonProperty("provincia")
            String provincia,
            @JsonProperty("codigo_postal")
            Integer cp
    ) {}


    public record UserDataRequest(String dni, Character genero, String telefono, String email){}

    public record AuthResponseWrapper(
            @JsonProperty("token") TokenData token
    ) {}

    public record TokenData(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") int expiresIn,
            @JsonProperty("token_type") String tokenType
    ) {}
}


