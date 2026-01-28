package com.boombet.boombet_backend.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public class AuthDTO {

    @Builder
    public record AuthResponseDTO(
            @NotNull
            String accessToken,
            @NotNull
            Object playerData,
            @NotNull
            String refreshToken,
            @JsonProperty("fcm_token")
            String fcmToken
    ) {
    }

    @Builder
    public record RefreshTokenRequestDTO(String refreshToken) {
    }

    ;
}
