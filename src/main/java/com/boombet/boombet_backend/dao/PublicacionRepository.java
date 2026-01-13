package com.boombet.boombet_backend.dao;

import com.boombet.boombet_backend.entity.Publicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    Page<Publicacion> findByParent_Id(Long parentId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario"})
    Page<Publicacion> findByUsuario_Id(Long userId, Pageable pageable);


    @EntityGraph(attributePaths = {"usuario"})
    Page<Publicacion> findByParentIsNull(Pageable pageable);

    //Para el Foro General (busca donde casinoGralId sea NULL y no sea respuesta)
    @EntityGraph(attributePaths = {"usuario"})
    Page<Publicacion> findByParentIsNullAndCasinoGralIdIsNull(Pageable pageable);

    //Para un Foro de Casino Específico (busca por ID y no sea respuesta)
    @EntityGraph(attributePaths = {"usuario"})
    Page<Publicacion> findByParentIsNullAndCasinoGralId(Long casinoGralId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario"})
    @Query("SELECT p FROM Publicacion p WHERE p.parent IS NULL AND " +
            "(p.casinoGralId IS NULL OR p.casinoGralId IN :allowedIds)")
    Page<Publicacion> findFeedForUser(@Param("allowedIds") List<Long> allowedIds, Pageable pageable);
    }
