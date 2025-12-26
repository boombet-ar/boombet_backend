package com.boombet.boombet_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UsuarioDTO {
    public record UsuarioResponse(
        Long id,
        String username,
        String dni,
        //Integer puntos,
        String email,
        @JsonProperty("bonda_enabled")
        Boolean bondaEnabled,
        @JsonProperty("is_deleted")
        Boolean isDeleted
    ){}
}
