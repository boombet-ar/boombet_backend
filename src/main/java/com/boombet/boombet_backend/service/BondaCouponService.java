package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.utils.CuponesUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class BondaCouponService {
    private final UsuarioRepository usuarioRepository;
    private final RestClient restClient;

    @Value("${bonda.api.key}")
    private String apiKey;

    @Value("${bonda.microsite.id}")
    private String micrositeId;

    public BondaCouponService(@Qualifier("bondaCouponsClient") RestClient restClient, UsuarioRepository usuarioRepository) {
        this.restClient = restClient;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene el listado de cupones filtrado y paginado.
     *
     * @param idUsuario ID del usuario (usado como codigo_afiliado).
     * @param page      Número de página (paginado de a 15).
     * @param orderBy   Ordenamiento: "relevant", "ownRelevant", "latest". (Puede ser null).
     * @return Map con la respuesta JSON de la API.
     */
    public Map<String, Object> obtenerCupones(Long idUsuario, Integer page, String orderBy, String query, Integer categoria) {
        int pageNum = (page != null && page > 0) ? page : 1;
        String sortOrder = (orderBy != null && !orderBy.isEmpty()) ? orderBy : "relevant";
        String codigoAfiliado = String.valueOf(idUsuario);

        try {
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/api/cupones")
                                .queryParam("key", apiKey)
                                .queryParam("micrositio_id", micrositeId)
                                .queryParam("codigo_afiliado", codigoAfiliado)
                                .queryParam("page", pageNum)
                                .queryParam("subcategories", false) // O true, según prefieras
                                .queryParam("with_locations", false)
                                .queryParam("orderBy", sortOrder);

                        if (query != null && !query.isEmpty()) {
                            builder.queryParam("query", query);
                        }

                        if (categoria != null && categoria > 0) {
                            builder.queryParam("categoria", categoria);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            /* Lógica de inyección de precios
            if (response != null && response.containsKey("results")) {
                Object resultsObj = response.get("results");
                if (resultsObj instanceof List) {
                    List<Map<String, Object>> cupones = (List<Map<String, Object>>) resultsObj;
                    cupones.forEach(CuponesUtils::injectarPrecioPuntos);
                }
            }
            */


            return response;

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
        String codigoAfiliado = String.valueOf(idUsuario);


        //String codigoAfiliado = "123456"; <-- codigo de afiliado para test

        try {
            Map<String, Object> cupon = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/cupones/{id}")
                            .queryParam("key", apiKey)
                            .queryParam("micrositio_id", micrositeId)
                            .queryParam("subcategories", false)
                            .queryParam("codigo_afiliado", codigoAfiliado)
                            .queryParam("subcategories", true)
                            .build(idCupon))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            /*
            if (cupon != null) {
                CuponesUtils.injectarPrecioPuntos(cupon);
            }

            Por ahora no usamos el sistema de puntos
            */
            return cupon;

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

        // 1. Buscar al Usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Obtener el cupón para saber su PRECIO
        // Reutilizamos tu método obtenerCuponPorId que ya calcula el precio usando CuponesUtils
        //Map<String, Object> cuponDetalle = obtenerCuponPorId(idUsuario, idCupon);

        // Asumimos que "precio_puntos" existe porque obtenerCuponPorId lo inyecta
        //Integer precio = (Integer) cuponDetalle.getOrDefault("precio_puntos", 1000);

        /*
        // 3. Validar saldo
        int puntosActuales = (usuario.getPuntos() != null) ? usuario.getPuntos() : 0;

        if (puntosActuales < precio) {
            throw new RuntimeException("Saldo insuficiente. Tienes " + puntosActuales + " puntos, pero el cupón cuesta " + precio + ".");
        }

        // 4. Descontar puntos (cobro preventivo)
        usuario.setPuntos(puntosActuales - precio);
        usuarioRepository.save(usuario);

        Por ahora no usamos el sistema de puntos
        */
        // --- LÓGICA DE BONDA ---
        //String codigoAfiliado = "123456"; <-- codigo de afiliado para test
        String codigoAfiliado = String.valueOf(idUsuario);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("key", apiKey);
        formData.add("micrositio_id", micrositeId);
        formData.add("codigo_afiliado", codigoAfiliado);

        if (externalId != null && !externalId.isEmpty()) {
            formData.add("external_id", externalId);
        }

        try {
            // 5. Intentar obtener el código
            Map<String, Object> response = restClient.post()
                    .uri("/api/cupones/{id}/codigo", idCupon)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            return response;

        } catch (Exception e) {
            // 6. ROLLBACK MANUAL: Si Bonda falla, le devolvemos los puntos al usuario
            System.err.println(">>> ❌ Error en Bonda. Devolviendo puntos al usuario...");
            //usuario.setPuntos(puntosActuales); // Restauramos el valor original. Descomentar cuando implementemos puntos
            //usuarioRepository.save(usuario);

            throw new RuntimeException("Error al solicitar el código (puntos devueltos): " + e.getMessage());
        }
    }

    /**
     * Obtiene los últimos 25 cupones solicitados por el afiliado.
     * GET /api/cupones_recibidos
     */
    public Map<String, Object> obtenerHistorialCupones(Long idUsuario) {

        //String codigoAfiliado = "123456"; <-- codigo de afiliado para test
        String codigoAfiliado = String.valueOf(idUsuario);

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


    /**
     * Devuelve las categorias de cupones disponibles
     * @param idUsuario
     * @return
     */
    public List<Map<String, Object>> obtenerCategorias(Long idUsuario) {
        String codigoAfiliado = String.valueOf(idUsuario);

        try {
            List<Map<String, Object>> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/categorias")
                            .queryParam("key", apiKey)
                            .queryParam("micrositio_id", micrositeId)
                            .queryParam("codigo_afiliado", codigoAfiliado)
                            .queryParam("subcategories", false)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            return response != null ? response : Collections.emptyList();

        } catch (Exception e) {
            System.err.println(">>> ❌ Error obteniendo categorías de Bonda: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
