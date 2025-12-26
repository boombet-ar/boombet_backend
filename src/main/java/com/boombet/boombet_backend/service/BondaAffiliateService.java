package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.entity.Usuario;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.Map;

@Service
public class BondaAffiliateService {

    private final RestClient restClient;
    private final UsuarioRepository usuarioRepository;

    @Value("${bonda.microsite.id}")
    private String micrositeId;

    public BondaAffiliateService(@Qualifier("bondaAffiliatesClient") RestClient restClient,
                        UsuarioRepository usuarioRepository) {
        this.restClient = restClient;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * POST: Crea un afiliado en Bonda usando el ID del Usuario como código.
     */
    public void crearAfiliado(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario));

        String code = String.valueOf(usuario.getId());
        Map<String, String> requestBody = Collections.singletonMap("code", code);

        try {
            // CAMBIO: En lugar de toBodilessEntity(), leemos el String para ver qué dice Bonda
            String responseBody = restClient.post()
                    .uri("api/v2/microsite/" + micrositeId + "/affiliates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            usuario.setBondaEnabled(true);
            System.out.println(">>> 📡 Respuesta RAW de Bonda: " + responseBody);
            System.out.println(">>> ✅ Afiliado procesado en Bonda. Code: " + code);

        } catch (Exception e) {
            System.err.println(">>> ❌ Error creando afiliado en Bonda: " + e.getMessage());
            if (e instanceof org.springframework.web.client.RestClientResponseException re) {
                System.err.println(">>> 📦 Cuerpo del error: " + re.getResponseBodyAsString());
            }
            throw new RuntimeException("Error al sincronizar con Bonda: " + e.getMessage());
        }
    }

    /**
     * DELETE: Elimina (soft-delete) un afiliado en Bonda.
     * La eliminación es temporal por 30 días, luego permanente.
     */
    public void eliminarAfiliado(Long idUsuario) {
        String code = String.valueOf(idUsuario);

        try {
            restClient.delete()
                    .uri("api/v2/microsite/" + micrositeId + "/affiliates/" + code)
                    .retrieve()
                    .toBodilessEntity();

            System.out.println(">>> 🗑️ Afiliado eliminado en Bonda (Soft Delete). Code: " + code);

        } catch (HttpClientErrorException.NotFound e) {
            System.err.println(">>> ⚠️ El usuario " + code + " no existía en Bonda o ya estaba eliminado.");
        } catch (Exception e) {
            System.err.println(">>> ❌ Error eliminando afiliado en Bonda: " + e.getMessage());
            throw new RuntimeException("Error al eliminar afiliado en Bonda: " + e.getMessage());
        }
    }

    /**
     * GET: Obtiene los datos de un afiliado por su código.
     * Retorna el Map con la respuesta o null si no existe.
     */
    public Map<String, Object> obtenerAfiliado(Long idUsuario) {
        String code = String.valueOf(idUsuario);

        try {
            // GET /api/v2/microsite/{microsite_id}/affiliates/{code}
            return restClient.get()
                    .uri("api/v2/microsite/" + micrositeId + "/affiliates/" + code)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        } catch (HttpClientErrorException.NotFound e) {
            System.out.println(">>> ℹ️ Afiliado no encontrado en Bonda (Code: " + code + ")");
            return null; // O lanzar excepción según prefieras
        } catch (Exception e) {
            System.err.println(">>> ❌ Error obteniendo afiliado de Bonda: " + e.getMessage());
            throw new RuntimeException("Error al obtener datos de Bonda: " + e.getMessage());
        }
    }
}