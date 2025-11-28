package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dto.WebsocketDTO;
import com.boombet.boombet_backend.websocket.ServerWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    private final ServerWebSocketHandler serverHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketService(ServerWebSocketHandler serverHandler) {
        this.serverHandler = serverHandler;
    }


    public void sendToWebSocket(WebsocketDTO request) {
        try {
            String link = request.getWebsocketLink();

            if (link == null || link.isEmpty()) return;

            // 2. Extraemos el ID del final del link
            // Asume que el ID está después de la última barra "/"
            String userId = link.substring(link.lastIndexOf('/') + 1);

            // 3. Preparamos el JSON a enviar (datos + respuestas)
            String jsonMessage = objectMapper.writeValueAsString(request);

            // 4. ¡ENVIAMOS! (No conectamos de nuevo, usamos la conexión existente)
            serverHandler.sendToUser(userId, jsonMessage);

        } catch (Exception e) {
            System.err.println("Error enviando notificación WS: " + e.getMessage());
        }
    }
}