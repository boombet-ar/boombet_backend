package com.boombet.boombet_backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

public class WebsocketHandler extends TextWebSocketHandler {

    private final Map<String, Object> payload;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebsocketHandler(Map<String, Object> payload) {
        this.payload = payload;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 1. Convertir mapa a JSON
        String jsonMessage = objectMapper.writeValueAsString(payload);

        // 2. Enviar mensaje
        session.sendMessage(new TextMessage(jsonMessage));

        // 3. Cerrar conexión
        session.close();
    }
}