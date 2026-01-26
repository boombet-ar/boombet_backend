package com.boombet.boombet_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Builder;

public class AfiliadorDTO {
    public record VerificationResponseDTO(
            boolean isTokenValid,
            String message
    ){}

    @Builder
    public record AfiliadorResponseDTO(
            Long id,
            String nombre,
            @JsonProperty("token_afiliador")
            String tokenAfiliador,
            @JsonProperty("cant_afiliaciones")
            Integer cantAfiliaciones,
            boolean activo,
            String email,
            String dni,
            String telefono
            //Falta implementar los eventos
    ){}

    public record AfiliadorRequestDTO(
            @Valid
            String nombre,
            String email,
            String dni,
            String telefono
    ){}


}
