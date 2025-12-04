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
            Map.entry("Indumentaria, Calzado y Moda", 1200),  // Indumentaria
            Map.entry("Belleza y Salud", 900),   // Belleza y Salud
            Map.entry("Servicios", 600),   // Servicios
            Map.entry("Cines", 800),   // Cines
            Map.entry("Gimnasios", 1000),  // Gimnasios
            Map.entry("Turismo", 3000),  // Turismo
            Map.entry("Gastronomía", 1100),  // Gastronomía
            Map.entry("Compras", 1000),  // Compras
            Map.entry("Inmobiliarias", 5000),  // Inmobiliarias
            Map.entry("Inmuebles", 5000),
            Map.entry("Tratamientos faciales", 900),
            Map.entry("Gimnasios y Deportes", 900)
    );

    public static Integer getPrecio(String categoryId) {
        return PRECIOS.getOrDefault(categoryId, 1000);
    }



    public static void injectarPrecioPuntos(Map<String, Object> cupon) {
        try {
            if (cupon.containsKey("categorias")) {
                Object categoriasObj = cupon.get("categorias");

                if (categoriasObj instanceof List) {
                    List<Map<String, Object>> categorias = (List<Map<String, Object>>) categoriasObj;

                    if (!categorias.isEmpty()) {
                        Map<String, Object> primeraCategoria = categorias.get(0);

                        Object nameObj = primeraCategoria.get("nombre");

                        if (nameObj != null) {
                            String catName = String.valueOf(nameObj);


                            Integer precio = CuponesUtils.getPrecio(catName);

                            cupon.put("precio_puntos", precio);
                        }
                    }
                }
            }

            // Si por alguna razón no se pudo calcular (sin categoría o nombre null), ponemos default
            if (!cupon.containsKey("precio_puntos")) {
                cupon.put("precio_puntos", 100000);
            }

        } catch (Exception e) {
            System.err.println("Error calculando precio para cupón: " + e.getMessage());
            cupon.put("precio_puntos", 1000000);
        }
    }
}