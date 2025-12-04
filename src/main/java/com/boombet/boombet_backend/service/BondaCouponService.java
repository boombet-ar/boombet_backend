package com.boombet.boombet_backend.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
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
                            .queryParam("subcategories", false)
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

    /**
     * Obtiene el detalle de un cupón específico por su ID.
     *
     * @param idUsuario ID del usuario (usado como codigo_afiliado).
     * @param idCupon   ID del cupón a buscar.
     * @return Map con la respuesta JSON de la API.
     */
    public Map<String, Object> obtenerCuponPorId(Long idUsuario, String idCupon) {

        // String codigoAfiliado = String.valueOf(idUsuario);
        String codigoAfiliado = "123456";

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/cupones/{id}")
                            .queryParam("key", apiKey)
                            .queryParam("micrositio_id", micrositeId)
                            .queryParam("codigo_afiliado", codigoAfiliado)
                            .queryParam("subcategories", true)
                            .build(idCupon))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            System.err.println(">>> ❌ Error obteniendo cupón " + idCupon + " de Bonda: " + e.getMessage());
            throw new RuntimeException("Error al obtener el cupón: " + e.getMessage());
        }
    }

    /**
     * Solicita el código de canje para un cupón específico.
     * POST /api/cupones/{id}/codigo
     *
     * @param idUsuario  ID del usuario que solicita el código.
     * @param idCupon    ID del cupón a canjear.
     * @param externalId (Opcional) ID externo para referencia.
     * @return Map con la respuesta que incluye el código (o error).
     */
    public Map<String, Object> generarCodigoCupon(Long idUsuario, String idCupon, String externalId) {


        /*
           FALTA LA LOGICA DE LOS PUNTOS
           SOLO DEBE PERMITIR RETIRAR UN CUPON SI EL USUARIO CUENTA CON LOS PUNTOS,
           Y DEBE RETIRARLE LOS PUNTOS CORRESPONDIENTES AL USUARIO.

         */
        // Mantenemos la lógica del ID de afiliado de prueba como en los métodos anteriores
        String codigoAfiliado = "123456";
        // En producción sería: String codigoAfiliado = String.valueOf(idUsuario);

        // Preparamos el cuerpo form-data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("key", apiKey);
        formData.add("micrositio_id", micrositeId);
        formData.add("codigo_afiliado", codigoAfiliado);

        if (externalId != null && !externalId.isEmpty()) {
            formData.add("external_id", externalId);
        }

        try {
            return restClient.post()
                    .uri("/api/cupones/{id}/codigo", idCupon)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            System.err.println(">>> ❌ Error solicitando código para cupón " + idCupon + ": " + e.getMessage());
            // Podrías relanzar una excepción personalizada o devolver un mapa de error
            throw new RuntimeException("Error al solicitar el código del cupón: " + e.getMessage());
        }
    }

    /**
     * Obtiene los últimos 25 cupones solicitados por el afiliado.
     * GET /api/cupones_recibidos
     */
    public Map<String, Object> obtenerHistorialCupones(Long idUsuario) {

        // Mantenemos el ID de prueba "123456" por consistencia con el resto del código actual.
        // En producción cambiar por: String.valueOf(idUsuario);
        String codigoAfiliado = "123456";

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/cupones_recibidos")
                            .queryParam("key", apiKey)
                            .queryParam("micrositio_id", micrositeId)
                            .queryParam("codigo_afiliado", codigoAfiliado)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            System.err.println(">>> ❌ Error obteniendo historial de cupones: " + e.getMessage());
            throw new RuntimeException("Error al obtener historial: " + e.getMessage());
        }
    }

}