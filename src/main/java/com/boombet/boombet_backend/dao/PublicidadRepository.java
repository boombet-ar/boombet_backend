// src/main/java/com/boombet/boombet_backend/dao/PublicidadRepository.java
package com.boombet.boombet_backend.dao;

import com.boombet.boombet_backend.entity.Publicidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PublicidadRepository extends JpaRepository<Publicidad, Long> {

    List<Publicidad> findByEndAtBefore(LocalDateTime now);
}