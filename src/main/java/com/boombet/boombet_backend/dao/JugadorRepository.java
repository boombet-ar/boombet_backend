package com.boombet.boombet_backend.dao;


import aj.org.objectweb.asm.commons.InstructionAdapter;
import com.boombet.boombet_backend.entity.Jugador;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    Optional<Jugador> findByEmail(String email);
    Optional<Jugador> findByDni(String dni);
}
