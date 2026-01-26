package com.boombet.boombet_backend.controller;

import com.boombet.boombet_backend.dto.AfiliadorDTO;
import com.boombet.boombet_backend.dto.PublicacionDTO;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.service.AfiliadorService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.boombet.boombet_backend.dto.AfiliadorDTO.VerificationResponseDTO;

@RestController
@RequestMapping("/api/afiliadores")
public class AfiliadorController {
    private final AfiliadorService afiliadorService;

    public AfiliadorController(AfiliadorService afiliadorService) {
        this.afiliadorService = afiliadorService;
    }

    /**
     *
     * @param pageable ?pag=n
     * @return
     */
    @GetMapping()
    public Page<AfiliadorDTO.AfiliadorResponseDTO> obtenerAfiliadores(
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return afiliadorService.getAfiliadores(pageable);
    }

    @PostMapping
    public ResponseEntity<AfiliadorDTO.AfiliadorResponseDTO> crearAfiliador(@RequestBody AfiliadorDTO.AfiliadorRequestDTO afiliadorDTO) {

        AfiliadorDTO.AfiliadorResponseDTO nuevoAfiliador = afiliadorService.crearAfiliador(afiliadorDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevoAfiliador);
    }

    @PatchMapping("/{id}/activo")
    public ResponseEntity<AfiliadorDTO.AfiliadorResponseDTO> toggleActivo(@PathVariable Long id) {

        AfiliadorDTO.AfiliadorResponseDTO afiliadorActualizado = afiliadorService.toggleActivo(id);

        return ResponseEntity.ok(afiliadorActualizado);
    }


}