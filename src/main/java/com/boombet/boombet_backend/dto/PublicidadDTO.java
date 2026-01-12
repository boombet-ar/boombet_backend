package com.boombet.boombet_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PublicidadDTO {

    @NotNull(message = "El ID del casino es obligatorio")
    private Integer casinoGralId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startAt;

    @NotNull(message = "La fecha de expiracion es obligatoria")
    private LocalDateTime endAt;

    private String mediaUrl;

    @NotBlank(message = "El texto es obligatorio")
    private String text;


    public PublicidadDTO() {
    }

    public PublicidadDTO(Integer casinoGralId, LocalDateTime startAt, LocalDateTime endAt, String mediaUrl, String text) {
        this.casinoGralId = casinoGralId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.mediaUrl = mediaUrl;
        this.text = text;
    }


}