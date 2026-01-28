package com.boombet.boombet_backend.dao;



import com.boombet.boombet_backend.entity.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String username);
    Optional<Usuario> findByJugador_Id(Long idJugador);
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
    Optional<Usuario>findByUsernameOrEmail(String username, String email);
    Optional<Usuario> findByVerificationToken(String token);

    Optional<Usuario> findByResetToken(String resetToken);
    Optional<Usuario> findByDni(String dni);

    boolean existsByUsername(String username);

    @Query("SELECT u.fcmToken FROM Usuario u WHERE u.fcmToken IS NOT NULL AND u.fcmToken != ''")
    List<String> findAllFcmTokens();

    /**
     * Desactiva 'bonda_enabled' de todos los usuarios que hayan cumplido el 'free trial' de 30 dias.
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE usuarios SET bonda_enabled = false WHERE created_at < NOW() - INTERVAL '30 days'", nativeQuery = true)
    void desactivarFreeTrialVencidos();
}
