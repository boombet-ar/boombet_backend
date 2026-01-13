package com.boombet.boombet_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;

public class PublicacionDTO {


    public record PublicacionRequestDTO(
            @NotBlank(message = "El contenido no puede estar vacio")
            String content,

            @JsonProperty("parent_id")
            Long parentId,

            @JsonProperty("casino_gral_id")
            Long casinoGralId
    ) {}

    @Builder
    public record PublicacionResponseDTO(
            Long id,
            String content,
            Long parentId,
            String username,
            Long casinoGralId,
            LocalDateTime createdAt,
            String userIconUrl

    ){}
}
