package com.boombet.boombet_backend.controller;

import com.boombet.boombet_backend.dto.AfiliadorDTO;
import com.boombet.boombet_backend.service.AfiliadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.boombet.boombet_backend.dto.AfiliadorDTO.VerificationResponseDTO;

@RestController
@RequestMapping("/api/afiliadores")
public class AfiliadorController {
    private final AfiliadorService afiliadorService;

    public AfiliadorController(AfiliadorService afiliadorService) {
        this.afiliadorService = afiliadorService;
    }



    // Crear afiliador

    // Habilitar/inhabilidar afiliador

}