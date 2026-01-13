package com.boombet.boombet_backend.dao;


import aj.org.objectweb.asm.commons.InstructionAdapter;
import com.boombet.boombet_backend.dto.CasinoDTO;
import com.boombet.boombet_backend.entity.Jugador;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    Optional<Jugador> findByEmail(String email);

    Optional<Jugador> findByDni(String dni);

    @Query(value = """
    SELECT 
        cg.id AS id,
        cg.nombre_gral AS nombreGral,
        c.url AS url,
        cg.logo_url AS logoUrl   
    FROM afiliaciones a
    INNER JOIN casinos c ON a.id_casino = c.id
    INNER JOIN casino_general cg ON c.casino_gral_id = cg.id
    WHERE a.id_jugador = :idJugador
    """, nativeQuery = true)
    List<CasinoDTO.casinosList> encontrarCasinosDelJugador(@Param("idJugador") Long idJugador);


    @Query(value = "SELECT DISTINCT c.casino_gral_id " +
            "FROM afiliaciones a " +
            "JOIN casinos c ON a.id_casino = c.id " +
            "WHERE a.id_jugador = :idJugador",
            nativeQuery = true)
    List<Long> findCasinoGralIdsByJugadorId(@Param("idJugador") Long idJugador); //Solo ids de los casinos del jugador
}

