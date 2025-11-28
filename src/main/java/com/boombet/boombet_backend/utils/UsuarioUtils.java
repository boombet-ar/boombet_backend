package com.boombet.boombet_backend.utils;


import com.boombet.boombet_backend.dto.UserDataRequestDTO;


public class UsuarioUtils {

    public static String generarCuit(UserDataRequestDTO input) {


        String dni = input.getDni();
        String sexo = input.getGenero();

        System.out.println(sexo);
        if (dni == null || dni.length() < 7 || dni.length() > 8) {
            throw new IllegalArgumentException("El DNI debe tener 7 u 8 dígitos.");
        }


        String dniNormalizado = String.format("%8s", dni).replace(' ', '0');


        boolean esMujer = sexo != null && (sexo.equalsIgnoreCase("Femenino") || sexo.equalsIgnoreCase("F"));

        int prefijo = esMujer ? 27 : 20;


        String cuitBase = prefijo + dniNormalizado;
        int verificador = calcularDigitoVerificador(cuitBase);


        if (verificador == 10) {
            prefijo = 23;
            cuitBase = prefijo + dniNormalizado;
            verificador = calcularDigitoVerificador(cuitBase);


            if (verificador == 10) {
                prefijo = 24;
                cuitBase = prefijo + dniNormalizado;
                verificador = calcularDigitoVerificador(cuitBase);
            }
        }


        return prefijo + dniNormalizado + verificador;
    }

    private static int calcularDigitoVerificador(String baseCuit) {
        int[] multiplicadores = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int suma = 0;


        for (int i = 0; i < 10; i++) {
            int digito = Character.getNumericValue(baseCuit.charAt(i));
            suma += digito * multiplicadores[i];
        }

        int resto = suma % 11;
        int resultado = 11 - resto;

        if (resultado == 11) return 0;
        return resultado;
    }

    /**
     * Valida la contraseña según las reglas estrictas de la imagen:
     * - No secuencias de teclado (qwerty).
     * - No números consecutivos (12, 23, 32).
     * - No números repetidos (11, 22).
     */
    public static void validarFormatoPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }

        String passLower = password.toLowerCase();

        // 1. Validar secuencias de teclado comunes (como dice el error "qwerty")
        // Puedes agregar más si es necesario (ej: "12345", "asdf")
        String[] secuenciasProhibidas = {"qwerty", "asdf", "zxcv"};
        for (String seq : secuenciasProhibidas) {
            if (passLower.contains(seq)) {
                throw new IllegalArgumentException("La contraseña no puede contener secuencias de teclado (" + seq + ").");
            }
        }

        // 2. Validar secuencias numéricas (Consecutivos y Repetitivos)
        // Recorremos la contraseña buscando pares de dígitos adyacentes
        for (int i = 0; i < password.length() - 1; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);

            // Solo analizamos si AMBOS caracteres son dígitos
            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                int n1 = Character.getNumericValue(c1);
                int n2 = Character.getNumericValue(c2);

                // Regla A: Repetitivos (ej: "22") -> (n1 == n2)
                if (n1 == n2) {
                    throw new IllegalArgumentException("La contraseña no puede contener números repetidos consecutivos (" + n1 + "" + n2 + ").");
                }

                // Regla B: Consecutivos Ascendentes (ej: "12") -> (n2 == n1 + 1)
                // Regla C: Consecutivos Descendentes (ej: "21") -> (n2 == n1 - 1)
                if (Math.abs(n1 - n2) == 1) {
                    throw new IllegalArgumentException("La contraseña no puede contener números consecutivos (" + n1 + "" + n2 + ").");
                }
            }
        }
    }

}
