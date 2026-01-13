package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.JugadorRepository;
import com.boombet.boombet_backend.dto.NotificacionDTO.NotificacionRequestDTO;
import com.boombet.boombet_backend.entity.Publicacion;
import com.boombet.boombet_backend.dao.PublicacionRepository;
import com.boombet.boombet_backend.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boombet.boombet_backend.dto.PublicacionDTO.PublicacionRequestDTO;
import com.boombet.boombet_backend.dto.PublicacionDTO.PublicacionResponseDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;

    private final FCMService fcmService;

    private final JugadorRepository jugadorRepository;
    @Transactional
    public PublicacionResponseDTO crearPublicacion(PublicacionRequestDTO dto, Usuario usuario){

        Publicacion publicacion = Publicacion.builder()
                .content(dto.content())
                .usuario(usuario)
                .casinoGralId(dto.casinoGralId())
                .build();

        if (dto.parentId() != null) { //Es respuesta?
            Publicacion publicacionPadre = publicacionRepository.findById(dto.parentId())
                    .orElseThrow(() -> new RuntimeException("No se puede responder a una publicación que no existe (ID: " + dto.parentId() + ")"));

            publicacion.setParent(publicacionPadre);

            try {
                Usuario usuarioPadre = publicacionPadre.getUsuario();
                Map<String, String> dataFCM = new HashMap<>();

                dataFCM.put("deeplink", "boombet://publicaciones/" + publicacionPadre.getId());

                NotificacionRequestDTO notificacion = NotificacionRequestDTO.builder()
                        .title("Boombet")
                        .body(usuario.getUsername() + " respondió a tu publicación!")
                        .data(dataFCM)
                        .build();

                fcmService.sendNotificationToUser(notificacion, usuarioPadre.getId());
            } catch (Exception e) {
                System.err.println("Error enviando push notification: " + e.getMessage());
            }
        }

        publicacionRepository.save(publicacion);

        return mapToDTO(publicacion);
    }


    @Transactional(readOnly = true)
    public PublicacionResponseDTO obtenerPorId(Long id) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada con ID: " + id));
        return mapToDTO(publicacion);
    }


    public PublicacionResponseDTO mapToDTO(Publicacion publicacion) {

        Usuario usuario = publicacion.getUsuario();

        return PublicacionResponseDTO.builder()
                .id(publicacion.getId())
                .content(publicacion.getContent())
                .parentId(publicacion.getParent() != null ? publicacion.getParent().getId() : null)
                .username(publicacion.getUsuario().getUsername())
                .createdAt(publicacion.getCreatedAt())
                .userIconUrl(usuario.getIconUrl())
                .casinoGralId(publicacion.getCasinoGralId())
                .build();
    }


    @Transactional(readOnly = true)
    public Page<PublicacionResponseDTO> listarRespuestas(Long parentId, Pageable pageable) {
        if (!publicacionRepository.existsById(parentId)) {
            throw new RuntimeException("La publicación padre no existe");
        }

        Page<Publicacion> paginaRespuestas = publicacionRepository.findByParent_Id(parentId, pageable);

        return paginaRespuestas.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PublicacionResponseDTO> listarPublicaciones(Usuario usuario, Long filtroCasinoId, Pageable pageable) {

        // 1. Obtener los IDs de casinos a los que el usuario está afiliado
        List<Long> misCasinosIds = jugadorRepository.findCasinoGralIdsByJugadorId(usuario.getJugador().getId());

        Page<Publicacion> pagina;

        if (filtroCasinoId != null) {
            // CASO A: El usuario quiere ver un casino específico

            // Validamos que esté afiliado a ese casino (Seguridad)
            if (!misCasinosIds.contains(filtroCasinoId)) {
                throw new RuntimeException("No tienes permiso para ver el foro de este casino o no estás afiliado.");
            }

            // Si tiene permiso, buscamos solo ese
            pagina = publicacionRepository.findByParentIsNullAndCasinoGralId(filtroCasinoId, pageable);

        } else {
            // CASO B: Feed completo (General + Mis Casinos)

            // Si la lista está vacía (no está afiliado a nada), el query IN podría fallar o no traer nada,
            // pero el OR IS NULL asegura que al menos vea el general.
            if (misCasinosIds.isEmpty()) {
                pagina = publicacionRepository.findByParentIsNullAndCasinoGralIdIsNull(pageable);
            } else {
                pagina = publicacionRepository.findFeedForUser(misCasinosIds, pageable);
            }
        }

        return pagina.map(this::mapToDTO);
    }

    @Transactional
    public void eliminarPublicacion(Usuario usuario, Long id) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        boolean esAutor = publicacion.getUsuario().getId().equals(usuario.getId());

        if (!esAutor) {
            throw new RuntimeException("No tienes permiso para eliminar esta publicación");
        }

        publicacionRepository.delete(publicacion);
    }

    public Page<PublicacionResponseDTO> verPublicacionesPorUsuario(Long userId, Pageable pageable) {
        Page<Publicacion> pagina = publicacionRepository.findByUsuario_Id(userId, pageable);
        return pagina.map(this::mapToDTO);
    }
}