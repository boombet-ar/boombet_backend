package com.boombet.boombet_backend.dao;

import com.boombet.boombet_backend.entity.Afiliador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AfiliadorRepository extends JpaRepository<Afiliador, Long> {
    Optional<Afiliador> findByTokenAfiliador(String tokenAfiliador);
}
