package com.boombet.boombet_backend.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String accessToken;
    private Object playerData;
    private String refreshToken;
    @JsonProperty("fcm_token")
    private String fcmToken;

    @Builder
    public record RefreshTokenRequestDTO(String refreshToken) {};
}
