package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.dto.NotificacionDTO.NotificacionRequestDTO;
import com.boombet.boombet_backend.entity.Usuario;
import com.google.firebase.messaging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FCMService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Envía una notificación push utilizando Firebase Cloud Messaging.
     * @param request -> Datos de la notificación
     * @param userId -> Id del usuario a notificar. Con este se extrae el FCM token para enviar la notificacion
     * @throws Exception -> Exception si falla el servicio de FCM o no se encuentra el usuario.
     */
    public void sendNotificationToUser(NotificacionRequestDTO request, Long userId) throws Exception {


        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        System.out.println("DEBUG USUARIO ID: " + usuario.getId());

        if (usuario.getFcmToken() == null || usuario.getFcmToken().isEmpty()) {
            throw new RuntimeException("El usuario " + usuario.getId() + " no tiene un dispositivo vinculado (Token NULL).");
        }

        Notification notification = Notification.builder()
                .setTitle(request.title())
                .setBody(request.body())
                .build();

        Message.Builder messageBuilder = Message.builder()
                .setToken(usuario.getFcmToken())
                .setNotification(notification);

        if (request.data() != null) {
            messageBuilder.putAllData(request.data());
        }


        FirebaseMessaging.getInstance().send(messageBuilder.build());
    }


    public void sendBroadcast(NotificacionRequestDTO request) throws FirebaseMessagingException {
        // 1. Obtener todos los tokens
        List<String> tokens = usuarioRepository.findAllFcmTokens();

        if (tokens.isEmpty()) {
            System.out.println("⚠️ No hay dispositivos registrados para notificar.");
            return;
        }

        System.out.println(">>> 📢 Iniciando Broadcast a " + tokens.size() + " dispositivos.");

        Notification notification = Notification.builder()
                .setTitle(request.title())
                .setBody(request.body())
                .build();

        // 3. Dividir en lotes de 500 (Límite de FCM)
        List<List<String>> batches = partition(tokens, 500);

        for (List<String> batch : batches) {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(notification)
                    .addAllTokens(batch)
                    .putAllData(request.data() != null ? request.data() : java.util.Collections.emptyMap()) // Data opcional
                    .build();

            // 4. Enviar lote
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            System.out.println("✅ Lote enviado: " + response.getSuccessCount() + " éxitos, " + response.getFailureCount() + " fallos.");
        }
    }

    // Utilidad para dividir listas
    private List<List<String>> partition(List<String> list, int size) {
        List<List<String>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

}