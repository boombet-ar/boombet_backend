package com.boombet.boombet_backend.controller;

import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.service.BondaCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cupones")
@RequiredArgsConstructor
public class BondaCouponController {

    private final BondaCouponService bondaCouponService;

    /**
     * Endpoint para obtener el listado de cupones.
     * * GET /api/cupones?page=1&orderBy=relevant
     * * @param usuario Usuario autenticado (inyectado automáticamente por Spring Security).
     * @param page Número de página (por defecto 1).
     * @param orderBy Criterio de ordenamiento (relevant, ownRelevant, latest). Por defecto "relevant".
     * @return JSON con la respuesta de Bonda (cupones y paginación).
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarCupones(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "relevant") String orderBy
    ) {
        if (usuario == null || usuario.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> respuesta = bondaCouponService.obtenerCupones(usuario.getId(), page, orderBy);

        return ResponseEntity.ok(respuesta);
    }

    //Visualizar un cupon
    @GetMapping("/id/{id}")
    public ResponseEntity<Map<String, Object>> getCuponById(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable("id") String id
    ) {
        if (usuario == null || usuario.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> respuesta = bondaCouponService.obtenerCuponPorId(usuario.getId(), id);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Canjear un cupon
     * @param usuario
     * @param id
     * @param body
     *
     */
    @PostMapping("/{id}/codigo")
    public ResponseEntity<Map<String, Object>> solicitarCodigoCupon(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable("id") String id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        if (usuario == null || usuario.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        String externalId = (body != null) ? body.get("external_id") : null;

        if (externalId == null || externalId.isEmpty()) {
            externalId = String.valueOf(usuario.getId());
        }

        Map<String, Object> respuesta = bondaCouponService.generarCodigoCupon(usuario.getId(), id, externalId);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Endpoint para ver el historial de canjes del usuario. Muestra los ultimos 25 cupones canjeados.
     * GET /api/cupones/recibidos
     */
    @GetMapping("/recibidos")

    public ResponseEntity<Map<String, Object>> verHistorialCupones(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> respuesta = bondaCouponService.obtenerHistorialCupones(usuario.getId());

        return ResponseEntity.ok(respuesta);
    }
}