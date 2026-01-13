package com.boombet.boombet_backend.controller;


import com.azure.core.annotation.Delete;
import com.boombet.boombet_backend.dto.PublicacionDTO.PublicacionRequestDTO;
import com.boombet.boombet_backend.dto.PublicacionDTO.PublicacionResponseDTO;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.service.PublicacionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {


    private final PublicacionService publicacionService;

    public PublicacionController(PublicacionService publicacionService) {
        this.publicacionService = publicacionService;
    }

    @PostMapping
    public ResponseEntity<PublicacionResponseDTO> crearPublicacion(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody PublicacionRequestDTO request
    ) {
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        PublicacionResponseDTO nuevaPublicacion = publicacionService.crearPublicacion(request, usuario);

        return ResponseEntity.ok(nuevaPublicacion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionResponseDTO> obtenerPublicacion(@PathVariable Long id) {
        PublicacionResponseDTO respuesta = publicacionService.obtenerPorId(id);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * @param casinoGralId id del casino del que queremos las publicaciones
     * @param pageable ?page=n?size=m
     * @return Devuelve las publicaciones paginadas
     */
    @GetMapping
    public ResponseEntity<Page<PublicacionResponseDTO>> verPublicaciones(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false, name="casino_id") Long  casinoGralId,
            @PageableDefault(size = 20, page = 0) Pageable pageable
    ) {
        Page<PublicacionResponseDTO> respuestas = publicacionService.listarPublicaciones(usuario, casinoGralId, pageable);
        return ResponseEntity.ok(respuestas);
    }



    /**
     *
     * @param pageable ?page=n?size=m
     * @return Devuelve las respuestas de una publicacion paginadas
     */
    @GetMapping("/{id}/respuestas")
    public ResponseEntity<Page<PublicacionResponseDTO>> verRespuestas(
            @PathVariable Long id,
            @PageableDefault(size = 20, page = 0) Pageable pageable
    ) {
        Page<PublicacionResponseDTO> respuestas = publicacionService.listarRespuestas(id, pageable);
        return ResponseEntity.ok(respuestas);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPublicacion(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long id
    ) {
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        publicacionService.eliminarPublicacion(usuario, id);

        return ResponseEntity.ok().build();
    }


    @GetMapping("/me")
    public ResponseEntity<Page<PublicacionResponseDTO>> verMisPublicaciones(
            @AuthenticationPrincipal Usuario usuario,
            @PageableDefault(size = 20, page = 0) Pageable pageable
    ) {
        Page<PublicacionResponseDTO> respuestas = publicacionService.verPublicacionesPorUsuario(usuario.getId(), pageable);
        return ResponseEntity.ok(respuestas);
    }
}
