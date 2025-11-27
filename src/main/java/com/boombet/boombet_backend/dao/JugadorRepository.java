package com.boombet.boombet_backend.dao;


import com.boombet.boombet_backend.entity.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JugadorRepository extends JpaRepository<Jugador, Long> {

}
