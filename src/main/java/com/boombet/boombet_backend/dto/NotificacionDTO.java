package com.boombet.boombet_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Map;

public class NotificacionDTO {

    @Builder
    public record NotificacionRequestDTO(
            //Long userId,
            @NotBlank(message = "El título es obligatorio")
            String title,
            @NotBlank(message = "Body es obligatorio")
            String body,
            Map<String, String> data
    ){}
}
