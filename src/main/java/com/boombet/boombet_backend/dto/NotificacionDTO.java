package com.boombet.boombet_backend.dto;

import java.util.Map;

public class NotificacionDTO {
    public record NotificacionRequestDTO(
            Long userId,
            String title,
            String body,
            Map<String, String> data
    ){}
}
