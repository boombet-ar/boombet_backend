package com.boombet.boombet_backend.controller;

import com.boombet.boombet_backend.dto.NotificacionDTO.NotificacionRequestDTO;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.service.FCMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class FCMController {

    @Autowired
    private FCMService fcmService;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@AuthenticationPrincipal Usuario usuario, @RequestBody NotificacionRequestDTO request) {
        Long userId = usuario.getId();

        try {
            fcmService.sendNotificationToUser(request, userId);

            return ResponseEntity.ok()
                    .body("✅ Notificación enviada correctamente al usuario ID: " + userId);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("❌ Error al enviar: " + e.getMessage());
        }
    }
}