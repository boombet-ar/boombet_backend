package com.boombet.boombet_backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;


    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async
    public void enviarCorreo(String destinatario, String asunto, String contenidoHtml) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(destinatario);
            helper.setSubject(asunto);

            helper.setText(contenidoHtml, true);

            javaMailSender.send(message);
            System.out.println(">>> 📧 Email HTML enviado a: " + destinatario);

        } catch (Exception e) {
            System.err.println(">>> ❌ Error enviando email: " + e.getMessage());
        }
    }
}