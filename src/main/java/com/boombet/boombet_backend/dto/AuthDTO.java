package com.boombet.boombet_backend.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public class AuthDTO {

    @Builder
    public record AuthResponseDTO(
        String accessToken,
         Object playerData,
        String refreshToken,
        @JsonProperty("fcm_token")
        String fcmToken
    ){}

    @Builder
    public record RefreshTokenRequestDTO(String refreshToken) {};
}
