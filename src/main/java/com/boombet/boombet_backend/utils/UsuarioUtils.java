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


}
