// src/main/java/com/boombet/boombet_backend/dao/PublicidadRepository.java
package com.boombet.boombet_backend.dao;

import com.boombet.boombet_backend.entity.Publicidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PublicidadRepository extends JpaRepository<Publicidad, Long> {

    List<Publicidad> findByEndAtBefore(LocalDateTime now);

    @Query("SELECT p FROM Publicidad p WHERE p.startAt <= :now AND p.endAt > :now")
    List<Publicidad> findActivePublicities(@Param("now") LocalDateTime now);
}