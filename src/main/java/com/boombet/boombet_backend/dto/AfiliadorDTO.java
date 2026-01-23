package com.boombet.boombet_backend.dto;

public class AfiliadorDTO {
    public record VerificationResponseDTO(
            boolean isTokenValid,
            String message
    ){}
}
