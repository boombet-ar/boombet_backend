package com.boombet.boombet_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    // Usamos el Logger oficial para que salga destacado en la consola
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.cloudmessaging.credentials}")
    private String firebaseJson;

    @PostConstruct
    public void initialize() {
        logger.info("⚡ Intentando inicializar Firebase...");

        try {
            //Validaciones
            if (firebaseJson == null || firebaseJson.isBlank()) {
                logger.error("ERROR: La variable 'firebase.cloudmessaging.credentials' está VACÍA o NULL.");
                return; // Detenemos aquí
            } else {
                logger.info("Variable de credenciales fcm encontrada.");
            }

            if (!FirebaseApp.getApps().isEmpty()) {
                logger.warn("⚠️ Firebase ya estaba inicializado. Saltando configuración.");
                return;
            }

            //Inicializa firebase
            InputStream serviceAccountStream = new ByteArrayInputStream(firebaseJson.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build();

            FirebaseApp.initializeApp(options);
            logger.info("Firebase Admin SDK inicializado.");

        } catch (Exception e) {
            logger.error("EXCEPCIÓN AL INICIAR FIREBASE: ", e);
        }
    }
}