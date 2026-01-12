package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.PublicidadRepository;
import com.boombet.boombet_backend.dto.PublicidadDTO;
import com.boombet.boombet_backend.entity.Publicidad;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicidadService {

    private final PublicidadRepository publicidadRepository;
    private final AzureBlobService azureBlobService;

    @Value("${spring.cloud.azure.storage.blob.container-name-publicidades}")
    private String containerPublicidad;

    private static final ZoneId ARGENTINA_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
    /**
     * Tarea programada para borrar publicidades expiradas y sus archivos multimedia asociados.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void borrarPublicidadesExpiradas() {
        LocalDateTime now = ZonedDateTime.now(ARGENTINA_ZONE).toLocalDateTime();
        System.out.println(">>> ⏰ Iniciando limpieza de publicidades expiradas a las: " + now);

        List<Publicidad> expiradas = publicidadRepository.findByEndAtBefore(now);

        if (expiradas.isEmpty()) {
            System.out.println(">>> ✅ No se encontraron publicidades expiradas.");
            return;
        }

        System.out.println(">>> 🗑️ Encontradas " + expiradas.size() + " publicidades para borrar.");

        for (Publicidad pub : expiradas) {
            try {

                azureBlobService.deleteBlob(pub.getMediaUrl(), containerPublicidad); //Ajustar

                publicidadRepository.delete(pub);
                System.out.println(">>> ✅ Borrado exitoso: ID=" + pub.getId() + ", URL=" + pub.getMediaUrl());

            } catch (Exception e) {
                System.err.println(">>> ❌ Error crítico procesando publicidad ID " + pub.getId() + ". La operación falló para este ítem. Causa: " + e.getMessage());
                throw new RuntimeException("Fallo la limpieza de publicidad: " + pub.getId(), e);
            }
        }
        System.out.println("Limpieza finalizada.");
    }

    public List<PublicidadDTO> obtenerPublicidadesActivas() { //Devuelve todas las que están dentro del rango horario
        LocalDateTime now = ZonedDateTime.now(ARGENTINA_ZONE).toLocalDateTime();

        List<Publicidad> activas = publicidadRepository.findActivePublicities(now);

        return activas.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PublicidadDTO> obtenerPublicidadesPorJugador(Long idJugador) {
        List<Publicidad> publicidades = publicidadRepository.findByJugadorAfiliaciones(idJugador);
        System.out.println(ZonedDateTime.now(ARGENTINA_ZONE).toLocalDateTime());
        return publicidades.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    private PublicidadDTO mapToDTO(Publicidad entidad) {
        return new PublicidadDTO(
                entidad.getCasinoGralId(),
                entidad.getStartAt(),
                entidad.getEndAt(),
                entidad.getMediaUrl(),
                entidad.getText()
        );
    }


}