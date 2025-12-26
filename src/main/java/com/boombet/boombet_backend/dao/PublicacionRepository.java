package com.boombet.boombet_backend.dao;

import com.boombet.boombet_backend.entity.Publicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;



public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    Page<Publicacion> findByParent_Id(Long parentId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario"})
    Page<Publicacion> findByUsuario_Id(Long userId, Pageable pageable);


    @EntityGraph(attributePaths = {"usuario"})
    Page<Publicacion> findByParentIsNull(Pageable pageable);
    }
