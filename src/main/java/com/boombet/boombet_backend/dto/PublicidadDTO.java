package com.boombet.boombet_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublicidadDTO {

    private Integer casinoGralId;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private String mediaUrl;

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