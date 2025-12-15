package com.boombet.boombet_backend.controller;

import com.boombet.boombet_backend.dto.JugadorDTO;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.service.JugadorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/jugadores")
public class JugadorController {

    private final JugadorService jugadorService;
    @GetMapping("/{id}")
    public ResponseEntity<JugadorDTO> getJugadorById(@PathVariable Long id) {
        JugadorDTO jugador = jugadorService.getJugador(id);

        if (jugador == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(jugador);
    }

    @PatchMapping("/update")
    public ResponseEntity<JugadorDTO> updateJugador(
            @AuthenticationPrincipal Usuario usuario, // <--- Aquí se inyecta la seguridad
            @RequestBody JugadorDTO dto) {

        JugadorDTO actualizado = jugadorService.actualizarJugador(usuario.getJugador().getId(), dto);

        return ResponseEntity.ok(actualizado);
    }
}
