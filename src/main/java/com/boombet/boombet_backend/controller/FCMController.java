package com.boombet.boombet_backend.controller;

import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.dto.NotificacionDTO.NotificacionRequestDTO;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.service.FCMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class FCMController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private FCMService fcmService;

    /**
     * Envia una notificación al usuario que hace la request.
     * @param usuario -> usuario extraido del jwt
     * @param request -> datos de la notificacion
     * @return
     */
    @PostMapping("/me")
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

    @Operation(summary = "Guardar Token FCM", description = "Actualiza el token de Firebase del usuario para notificaciones push. " +
            "BODY: { \"token\":\"token fcm\"}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token guardado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido o faltante", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario no autenticado", content = @Content)
    })

    @PostMapping("/save_fcmtoken")
    public ResponseEntity<?> saveFCMToken(@AuthenticationPrincipal Usuario usuario, @RequestBody Map<String, String> token ) {
        usuario.setFcmToken(token.get("token"));
        usuarioRepository.save(usuario);
        return ResponseEntity.ok().build();
    }


}