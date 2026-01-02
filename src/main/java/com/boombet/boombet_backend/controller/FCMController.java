package com.boombet.boombet_backend.controller;

import com.boombet.boombet_backend.dto.NotificacionDTO.NotificacionRequestDTO;
import com.boombet.boombet_backend.service.FCMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class FCMController {

    @Autowired
    private FCMService fcmService;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificacionRequestDTO request) {
        try {
            fcmService.sendNotificationToUser(request);

            return ResponseEntity.ok()
                    .body("✅ Notificación enviada correctamente al usuario ID: " + request.userId());

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("❌ Error al enviar: " + e.getMessage());
        }
    }
}