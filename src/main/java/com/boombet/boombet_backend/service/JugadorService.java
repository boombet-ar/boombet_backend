package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.JugadorRepository;
import com.boombet.boombet_backend.dto.AffiliationDTO;
import com.boombet.boombet_backend.dto.JugadorDTO;
import com.boombet.boombet_backend.entity.Jugador;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class JugadorService {

    private final JugadorRepository jugadorRepository;

    public Jugador crearJugador(AffiliationDTO userData) {
        Jugador jugador = Jugador.builder()
                .nombre(userData.getNombre())
                .apellido(userData.getApellido())
                .email(userData.getEmail())
                .telefono(userData.getTelefono())
                .genero(userData.getGenero())
                .fecha_nacimiento(userData.getFechaNacimiento())
                .dni(userData.getDni())
                .cuit(userData.getCuit())
                .est_civil(userData.getEstCivil())
                .calle(userData.getCalle())
                .numcalle(userData.getNumCalle())
                .provincia(userData.getProvincia())
                .ciudad(userData.getCiudad())
                .cp(userData.getCp())
                .build();

        return jugadorRepository.save(jugador);
    }

    @PreAuthorize("authentication.principal.jugador?.id == #id")
    public JugadorDTO getJugador(Long id) {
        Jugador jugador = jugadorRepository.findById(id).orElse(null);
        JugadorDTO jugadorDto = mapToDto(jugador);
        return jugadorDto;
    }

    @PreAuthorize("authentication.principal.jugador?.id == #id")
    public JugadorDTO actualizarJugador(Long id, JugadorDTO dto) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        if (dto.getNombre() != null) { jugador.setNombre(dto.getNombre()); }
        if (dto.getApellido() != null) { jugador.setApellido(dto.getApellido()); }
        if (dto.getEmail() != null) { jugador.setEmail(dto.getEmail()); }
        if (dto.getTelefono() != null) { jugador.setTelefono(dto.getTelefono()); }
        if (dto.getGenero() != null) { jugador.setGenero(dto.getGenero()); }
        if (dto.getCuit() != null) { jugador.setCuit(dto.getCuit()); }
        if (dto.getEst_civil() != null) { jugador.setEst_civil(dto.getEst_civil()); }

        if (dto.getCalle() != null) { jugador.setCalle(dto.getCalle()); }
        if (dto.getNumcalle() != null) { jugador.setNumcalle(dto.getNumcalle()); }
        if (dto.getProvincia() != null) { jugador.setProvincia(dto.getProvincia()); }
        if (dto.getCiudad() != null) { jugador.setCiudad(dto.getCiudad()); }
        if (dto.getCp() != null) { jugador.setCp(dto.getCp()); }

        JugadorDTO jugadorActualizado = mapToDto(jugador);
        jugadorRepository.save(jugador);

        return jugadorActualizado;
    }


    private JugadorDTO mapToDto(Jugador j) {
        return JugadorDTO.builder()
                .id(j.getId())
                .nombre(j.getNombre())
                .apellido(j.getApellido())
                .email(j.getEmail())
                .telefono(j.getTelefono())
                .genero(j.getGenero())
                .fecha_nacimiento(j.getFecha_nacimiento())
                .dni(j.getDni())
                .cuit(j.getCuit())
                .est_civil(j.getEst_civil())
                .calle(j.getCalle())
                .numcalle(j.getNumcalle())
                .provincia(j.getProvincia())
                .ciudad(j.getCiudad())
                .cp(j.getCp())
                .build();
    }
}


