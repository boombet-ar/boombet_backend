package com.boombet.boombet_backend.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class BondaCouponService {

    private final RestClient restClient;

    @Value("${bonda.api.key}")
    private String apiKey;

    @Value("${bonda.microsite.id}")
    private String micrositeId;

    public BondaCouponService(@Qualifier("bondaCouponsClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Obtiene el listado de cupones filtrado y paginado.
     *
     * @param idUsuario ID del usuario (usado como codigo_afiliado).
     * @param page      Número de página (paginado de a 15).
     * @param orderBy   Ordenamiento: "relevant", "ownRelevant", "latest". (Puede ser null).
     * @return Map con la respuesta JSON de la API.
     */
    public Map<String, Object> obtenerCupones(Long idUsuario, Integer page, String orderBy) {
        int pageNum = (page != null && page > 0) ? page : 1;
        String sortOrder = (orderBy != null && !orderBy.isEmpty()) ? orderBy : "relevant";
        //String codigoAfiliado = String.valueOf(idUsuario);

        String codigoAfiliado = "123456"; //Por ahora usamos este para testear


        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/cupones") // Asume que la base-url no incluye /api/v2 estricto, o ajusta según corresponda
                            .queryParam("key", apiKey)
                            .queryParam("micrositio_id", micrositeId)
                            .queryParam("codigo_afiliado", codigoAfiliado)
                            .queryParam("page", pageNum)
                            .queryParam("subcategories", true)
                            .queryParam("with_locations", false)
                            .queryParam("orderBy", sortOrder)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            System.err.println(">>> ❌ Error obteniendo cupones de Bonda: " + e.getMessage());
            throw new RuntimeException("Error al obtener cupones: " + e.getMessage());
        }
    }
}