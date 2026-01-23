package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.AfiliadorRepository;
import com.boombet.boombet_backend.entity.Afiliador;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@AllArgsConstructor
public class AfiliadorService {

    @Autowired
    private final AfiliadorRepository afiliadorRepository;

    public boolean verificarTokenAfiliador(String tokenAfiliador) {
        Optional<Afiliador> afiliador = afiliadorRepository.findByTokenAfiliador(tokenAfiliador);
        return afiliador.isPresent();
    }


}


