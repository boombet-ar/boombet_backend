package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.AfiliadorRepository;
import com.boombet.boombet_backend.dto.AfiliadorDTO;
import com.boombet.boombet_backend.dto.AfiliadorDTO.AfiliadorRequestDTO;
import com.boombet.boombet_backend.dto.AfiliadorDTO.AfiliadorResponseDTO;
import com.boombet.boombet_backend.entity.Afiliador;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
@AllArgsConstructor
public class AfiliadorService {

    @Autowired
    private final AfiliadorRepository afiliadorRepository;

    public boolean verificarTokenAfiliador(String tokenAfiliador) {
        String tokenMayus = tokenAfiliador.toUpperCase();
        Optional<Afiliador> afiliador = afiliadorRepository.findByTokenAfiliador(tokenMayus);
        return afiliador.isPresent();
    }

    public AfiliadorResponseDTO crearAfiliador(AfiliadorRequestDTO afiliadorDTO){
        Afiliador afiliador = Afiliador.builder()
                .nombre(afiliadorDTO.nombre())
                .telefono(afiliadorDTO.telefono())
                .dni(afiliadorDTO.dni())
                .tokenAfiliador(crearTokenAfiliador())
                .activo(true)
                .cantAfiliaciones(0)
                .email(afiliadorDTO.email())
                .build();

        afiliadorRepository.save(afiliador);

        return mapToDto(afiliador);

    }

    public Page<AfiliadorResponseDTO> getAfiliadores(Pageable pageable) {
        return afiliadorRepository.findAll(pageable)
                                  .map(this::mapToDto);
    }

    public AfiliadorResponseDTO mapToDto(Afiliador afiliador){
        AfiliadorResponseDTO afiliadorDto = AfiliadorResponseDTO.builder()
                .nombre(afiliador.getNombre())
                .tokenAfiliador(afiliador.getTokenAfiliador())
                .id(afiliador.getId())
                .activo(afiliador.isActivo())
                .dni(afiliador.getDni())
                .telefono(afiliador.getTelefono())
                .cantAfiliaciones(afiliador.getCantAfiliaciones())
                .email(afiliador.getEmail())
                .build();

        return afiliadorDto;
    }


    @Transactional
    public AfiliadorResponseDTO toggleActivo(Long id) {
        Afiliador afiliador = afiliadorRepository.findById(id)
                                                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Afiliador no encontrado"));

        afiliador.setActivo(!afiliador.isActivo());

        afiliadorRepository.save(afiliador);

        return mapToDto(afiliador);
    }

    public String crearTokenAfiliador() {
        String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom random = new SecureRandom();

        String parte1 = IntStream.range(0, 3)
                                 .mapToObj(i -> String.valueOf(letras.charAt(random.nextInt(letras.length()))))
                                 .collect(Collectors.joining());

        String parte2 = IntStream.range(0, 3)
                                 .mapToObj(i -> String.valueOf(letras.charAt(random.nextInt(letras.length()))))
                                 .collect(Collectors.joining());

        String token = parte1 + "-" + parte2;

        if(afiliadorRepository.findByTokenAfiliador(token).isPresent()){
            return crearTokenAfiliador();
        } else{
            return token;
        }
    }

    public void deleteAfiliadorById(Long id) {
        afiliadorRepository.deleteById(id);
    }

}


