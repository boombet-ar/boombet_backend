package com.boombet.boombet_backend.config;

import com.boombet.boombet_backend.websocket.ServerWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // CAMBIO CLAVE: Usamos "/affiliation/*" para aceptar cualquier ID detrás
        registry.addHandler(serverWebSocketHandler(), "/affiliation/*")
                .setAllowedOrigins("*");
    }

    @Bean
    public ServerWebSocketHandler serverWebSocketHandler() {
        return new ServerWebSocketHandler();
    }
}