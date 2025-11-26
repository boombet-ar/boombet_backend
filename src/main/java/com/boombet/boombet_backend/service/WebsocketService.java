package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dto.WebsocketDTO;

import com.boombet.boombet_backend.websocket.WebsocketHandler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebsocketService {

    @Async
    public void sendToWebSocket(WebsocketDTO request) {
        if (request.getWebsocketLink() == null || request.getWebsocketLink().isEmpty()) {
            return;
        }

        try {
            WebSocketClient client = new StandardWebSocketClient();

            // Crear el payload limpio (sin el link)
            Map<String, Object> payloadToSend = new HashMap<>();
            payloadToSend.put("playerData", request.getPlayerData());
            payloadToSend.put("responses", request.getResponses());

            // Instanciar handler y conectar
            WebsocketHandler handler = new WebsocketHandler(payloadToSend);
            URI uri = URI.create(request.getWebsocketLink());

            client.execute(handler, new WebSocketHttpHeaders(), uri).get();

        } catch (Exception e) {
            System.err.println("Error en WebSocketService: " + e.getMessage());
        }
    }
}