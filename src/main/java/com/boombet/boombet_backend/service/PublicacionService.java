package com.boombet.boombet_backend.service;

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


@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;


    @Transactional
    public PublicacionResponseDTO crearPublicacion(PublicacionRequestDTO dto, Usuario usuario) {

        Publicacion publicacion = Publicacion.builder()
                .content(dto.content())
                .usuario(usuario)
                .build();

        if (dto.parentId() != null) {
            Publicacion publicacionPadre = publicacionRepository.findById(dto.parentId())
                    .orElseThrow(() -> new RuntimeException("No se puede responder a una publicación que no existe (ID: " + dto.parentId() + ")"));

            publicacion.setParent(publicacionPadre);
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

        return PublicacionResponseDTO.builder()
                .id(publicacion.getId())
                .content(publicacion.getContent())
                .parentId(publicacion.getParent() != null ? publicacion.getParent().getId() : null)
                .username(publicacion.getUsuario().getUsername())
                .createdAt(publicacion.getCreatedAt())
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
    public Page<PublicacionResponseDTO> listarPublicaciones(Pageable pageable) {
        Page<Publicacion> pagina = publicacionRepository.findByParentIsNull(pageable);

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
}