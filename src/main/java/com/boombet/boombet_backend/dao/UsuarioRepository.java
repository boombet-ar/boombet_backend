package com.boombet.boombet_backend.dao;



import com.boombet.boombet_backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String username);
    Optional<Usuario> findByJugador_Id(Long idJugador);
    boolean existsByEmail(String email);
    Optional<Usuario>findByUsernameOrEmail(String username, String email);
}
