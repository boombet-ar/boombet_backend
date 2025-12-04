package com.boombet.boombet_backend.utils;

import java.util.Map;

public class CuponesUtils {

    // Diccionario estático: ID de Categoría -> Precio en Puntos
    private static final Map<String, Integer> PRECIOS = Map.ofEntries(
            Map.entry("126", 1500), // Motos
            Map.entry("64", 2000),  // Autos
            Map.entry("71", 800),   // Teatros
            Map.entry("74", 1000),  // Entretenimientos
            Map.entry("68", 500),   // Educación
            Map.entry("69", 1200),  // Indumentaria
            Map.entry("65", 900),   // Belleza y Salud
            Map.entry("73", 600),   // Servicios
            Map.entry("70", 800),   // Cines
            Map.entry("67", 1000),  // Gimnasios
            Map.entry("63", 3000),  // Turismo
            Map.entry("66", 1100),  // Gastronomía
            Map.entry("72", 1000),  // Compras
            Map.entry("14", 5000),  // Inmobiliarias
            Map.entry("15", 5000)   // Inmuebles
    );

    public static Integer getPrecio(String categoryId) {
        // Devuelve el precio del mapa, o 1000 por defecto si la categoría es nueva y no está en la lista
        return PRECIOS.getOrDefault(categoryId, 1000);
    }
}