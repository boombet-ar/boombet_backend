// src/main/java/com/boombet/boombet_backend/service/PublicidadService.java
package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.PublicidadRepository;
import com.boombet.boombet_backend.entity.Publicidad;
import jakarta.transaction.Transactional; // Importar jakarta.transaction
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicidadService {

    private final PublicidadRepository publicidadRepository;
    private final AzureBlobService azureBlobService;

    /**
     * Tarea programada para borrar publicidades expiradas y sus archivos multimedia asociados.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional // Garantiza que si falla el borrado de Azure O la DB, la operación es ATÓMICA.
    public void borrarPublicidadesExpiradas() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(">>> ⏰ Iniciando limpieza de publicidades expiradas a las: " + now);

        List<Publicidad> expiradas = publicidadRepository.findByEndAtBefore(now);

        if (expiradas.isEmpty()) {
            System.out.println(">>> ✅ No se encontraron publicidades expiradas.");
            return;
        }

        System.out.println(">>> 🗑️ Encontradas " + expiradas.size() + " publicidades para borrar.");

        for (Publicidad pub : expiradas) {
            try {

                azureBlobService.deleteBlob(pub.getMediaUrl()); //Ajustar

                publicidadRepository.delete(pub);
                System.out.println(">>> ✅ Borrado exitoso: ID=" + pub.getId() + ", URL=" + pub.getMediaUrl());

            } catch (Exception e) {
                System.err.println(">>> ❌ Error crítico procesando publicidad ID " + pub.getId() + ". La operación falló para este ítem. Causa: " + e.getMessage());
                throw new RuntimeException("Fallo la limpieza de publicidad: " + pub.getId(), e);
            }
        }
        System.out.println("Limpieza finalizada.");
    }
}