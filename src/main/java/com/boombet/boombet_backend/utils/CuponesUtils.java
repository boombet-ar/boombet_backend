package com.boombet.boombet_backend.utils;

import java.util.List;
import java.util.Map;

public class CuponesUtils {

    // Diccionario estático: ID de Categoría -> Precio en Puntos
    private static final Map<String, Integer> PRECIOS = Map.ofEntries(
            Map.entry("Motos", 1500), // Motos
            Map.entry("Autos", 2000),  // Autos
            Map.entry("Teatros", 800),   // Teatros
            Map.entry("Entretenimientos", 1000),  // Entretenimientos
            Map.entry("Educación", 500),   // Educación
            Map.entry("Indumentaria", 1200),  // Indumentaria
            Map.entry("Belleza y Salud", 900),   // Belleza y Salud
            Map.entry("Servicios", 600),   // Servicios
            Map.entry("Cines", 800),   // Cines
            Map.entry("Gimnasios", 1000),  // Gimnasios
            Map.entry("Turismo", 3000),  // Turismo
            Map.entry("Gastronomía", 1100),  // Gastronomía
            Map.entry("Compras", 1000),  // Compras
            Map.entry("Inmobiliarias", 5000),  // Inmobiliarias
            Map.entry("Inmuebles", 5000)
    );

    public static Integer getPrecio(String categoryId) {
        // Devuelve el precio del mapa, o 1000 por defecto si la categoría es nueva y no está en la lista
        return PRECIOS.getOrDefault(categoryId, 1000);
    }



    public static void injectarPrecioPuntos(Map<String, Object> cupon) {
        try {
            if (cupon.containsKey("categorias")) {
                Object categoriasObj = cupon.get("categorias");

                if (categoriasObj instanceof List) {
                    List<Map<String, Object>> categorias = (List<Map<String, Object>>) categoriasObj;

                    if (!categorias.isEmpty()) {
                        // Tomamos la primera categoría disponible
                        Map<String, Object> primeraCategoria = categorias.get(0);
                        Object idObj = primeraCategoria.get("id"); // Puede venir como Integer o String

                        if (idObj != null) {
                            String catId = String.valueOf(idObj);

                            // Usamos tu Utils existente
                            Integer precio = CuponesUtils.getPrecio(catId);

                            // Agregamos el nuevo campo al mapa del cupón
                            cupon.put("precio_puntos", precio);
                        }
                    }
                }
            }

            // Si por alguna razón no se pudo calcular (sin categoría), ponemos default
            if (!cupon.containsKey("precio_puntos")) {
                cupon.put("precio_puntos", 1000); // Default seguro
            }

        } catch (Exception e) {
            System.err.println("Error calculando precio para cupón: " + e.getMessage());
            cupon.put("precio_puntos", 1000); // Fallback en caso de error de estructura
        }
    }
}