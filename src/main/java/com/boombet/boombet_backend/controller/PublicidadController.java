package com.boombet.boombet_backend.controller;

import com.boombet.boombet_backend.dto.NotificacionDTO;
import com.boombet.boombet_backend.dto.PublicidadDTO;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.service.FCMService;
import com.boombet.boombet_backend.service.PublicidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@RestController
@RequestMapping("/api/publicidades")
public class PublicidadController {

    private final PublicidadService publicidadService;
    private final FCMService fcmService;

    public PublicidadController(PublicidadService publicidadService, FCMService fcmService) {
        this.publicidadService = publicidadService;
        this.fcmService = fcmService;
    }

    @GetMapping
    public ResponseEntity<List<PublicidadDTO>> verTodasLasPublicidades() {
        List<PublicidadDTO> lista = publicidadService.obtenerPublicidadesActivas();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/me")
    public ResponseEntity<List<PublicidadDTO>> obtenerMisPublicidades(@AuthenticationPrincipal Usuario usuario) {

        if (usuario == null || usuario.getJugador() == null) {
            return ResponseEntity.badRequest().build();
        }

        Long idJugador = usuario.getJugador().getId();
        List<PublicidadDTO> misPublicidades = publicidadService.obtenerPublicidadesPorJugador(idJugador);

        return ResponseEntity.ok(misPublicidades);
    }


    @Value("${security.custom.header-token}")
    private String expectedToken;

    @PostMapping("/nueva_publicidad_notif")
    public ResponseEntity<PublicidadDTO> nuevaPublicidadNotif(@RequestHeader(value = "key", required = true) String apiKey,
                                                              @RequestBody @Valid PublicidadDTO publicidad) throws Exception{


        System.out.println(">>> DEBUG AZURE:");
        System.out.println("1. Token recibido (Header): '" + apiKey + "'");
        System.out.println("2. Token esperado (Env): '" + expectedToken + "'");

        if (expectedToken == null) {
            System.out.println("3. ERROR: La variable expectedToken es NULL. Revisa application.properties en Azure.");
        }
        // ---------------------

        if (expectedToken == null || !expectedToken.equals(apiKey)) {
            System.out.println(">>> RECHAZADO: Retornando 403");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        if (!expectedToken.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        NotificacionDTO.NotificacionRequestDTO notif = NotificacionDTO.NotificacionRequestDTO.builder()
                        .title("Boombet")
                        .body(publicidad.getText())
                        .build();

        PublicidadDTO response = PublicidadDTO.builder()
                .text(publicidad.getText())
                .startAt(publicidad.getStartAt())
                .casinoGralId(publicidad.getCasinoGralId())
                .mediaUrl(publicidad.getMediaUrl())
                .endAt(publicidad.getEndAt())
                .build();

        fcmService.sendBroadcast(notif);
        return ResponseEntity.ok(response);
    }
}
