package com.boombet.boombet_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;


    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async
    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(destinatario);
            message.setSubject(asunto);
            message.setText(cuerpo);

            javaMailSender.send(message);
            System.out.println(">>> 📧 Email enviado exitosamente a: " + destinatario);

        } catch (Exception e) {
            System.err.println(">>> ❌ Error enviando email: " + e.getMessage());
        }
    }
}