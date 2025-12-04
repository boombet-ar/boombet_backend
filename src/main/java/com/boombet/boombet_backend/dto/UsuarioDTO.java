package com.boombet.boombet_backend.dto;

public class UsuarioDTO {
    public record UsuarioResponse(
        Long id,
        String username,
        String dni,
        //Integer puntos,
        String email
    ){}
}
