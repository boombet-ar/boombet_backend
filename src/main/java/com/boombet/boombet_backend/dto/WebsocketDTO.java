package com.boombet.boombet_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class WebsocketDTO {

    private Map<String, Object> playerData;

    private Map<String, Object> responses;

    @JsonProperty("websocketLink")
    private String websocketLink;

}