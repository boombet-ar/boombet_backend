package com.boombet.boombet_backend.dto;

import lombok.Data;

@Data
public class RegistroRequestDTO {
    private UserDataDTO userData;
    private String websocketLink;
}
