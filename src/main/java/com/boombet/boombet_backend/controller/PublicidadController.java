package com.boombet.boombet_backend.controller;

import com.boombet.boombet_backend.dto.PublicidadDTO;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.service.PublicidadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/publicidades")
public class PublicidadController {

    private final PublicidadService publicidadService;

    public PublicidadController(PublicidadService publicidadService) {
        this.publicidadService = publicidadService;
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

}
