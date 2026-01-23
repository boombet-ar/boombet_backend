package com.boombet.boombet_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistroRequestDTO {

    @Valid
    @NotNull(message = "Los datos del jugador son obligatorios")
    @JsonProperty("playerData")
    private AffiliationDTO confirmedData;

    private String websocketLink;
}
