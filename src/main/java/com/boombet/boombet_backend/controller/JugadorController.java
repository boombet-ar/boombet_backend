package com.boombet.boombet_backend.controller;

import com.boombet.boombet_backend.dto.JugadorDTO;
import com.boombet.boombet_backend.service.JugadorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PatchMapping("/update/{id}")
    public ResponseEntity<JugadorDTO> updateJugador(
            @PathVariable Long id,
            @RequestBody JugadorDTO jugadorDto) {

        JugadorDTO jugadorActualizado = jugadorService.actualizarJugador(id, jugadorDto);

        return ResponseEntity.ok(jugadorActualizado);
    }
}
