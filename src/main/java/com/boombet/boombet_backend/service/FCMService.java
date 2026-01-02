package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.dto.NotificacionDTO.NotificacionRequestDTO;
import com.boombet.boombet_backend.entity.Usuario;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FCMService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método principal: Enviar notificación buscando al usuario por ID
    public void sendNotificationToUser(NotificacionRequestDTO request, Long userId) throws Exception {

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        if (usuario.getFcmToken() == null || usuario.getFcmToken().isEmpty()) {
            throw new RuntimeException("El usuario " + usuario.getId() + " no tiene un dispositivo vinculado (Token NULL).");
        }

        // 3. Construimos la notificación visual
        Notification notification = Notification.builder()
                .setTitle(request.title())
                .setBody(request.body())
                .build();

        Message.Builder messageBuilder = Message.builder()
                .setToken(usuario.getFcmToken())
                .setNotification(notification);

        // Agregamos datos extra si vienen
        if (request.data() != null) {
            messageBuilder.putAllData(request.data());
        }

        // 5. Enviamos
        FirebaseMessaging.getInstance().send(messageBuilder.build());
    }
}