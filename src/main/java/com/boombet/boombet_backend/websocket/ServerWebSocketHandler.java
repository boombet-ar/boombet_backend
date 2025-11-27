package com.boombet.boombet_backend.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerWebSocketHandler extends TextWebSocketHandler {

    // Mapa para guardar: "176426..." -> Sesión
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extraemos el ID de la URL: /affiliation/1764265985718
        String path = session.getUri().getPath();
        String id = path.substring(path.lastIndexOf('/') + 1);

        sessions.put(id, session);
        System.out.println(">>> Cliente conectado ID: " + id);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Limpieza: quitamos la sesión cuando se desconecta
        sessions.values().remove(session);
        System.out.println(">>> Cliente desconectado: " + session.getId());
    }

    // En ServerWebSocketHandler.java

    public void sendToUser(String targetId, String message) {
        // Log para ver a quién buscamos
        System.out.println(">>> 🔍 Buscando sesión para ID: " + targetId);

        WebSocketSession session = sessions.get(targetId);

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                System.out.println(">>> ✅ ENVIADO a: " + targetId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(">>> ❌ FALLO: No existe sesión para " + targetId);

            // IMPRIMIR TODOS LOS QUE SÍ ESTÁN CONECTADOS
            System.out.println(">>> 📋 Lista de conectados reales: " + sessions.keySet());
        }
    }
}