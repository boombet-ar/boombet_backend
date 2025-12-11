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

    public static String construirEmailBienvenida(String nombre, String link) {
        // Colores extraídos de las referencias:
        String verdeNeon = "#4CE68B"; // El verde vibrante de los botones
        String darkBgMain = "#09120f"; // Fondo principal muy oscuro
        String darkBgCard = "#11211c"; // Fondo de la tarjeta (un poco menos oscuro)
        String textColorPrimary = "#ffffff"; // Texto blanco
        String textColorSecondary = "#b7c4bd"; // Texto gris claro con tinte verde

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Bienvenido a BoomBet</title>
                    <style>
                        /* Estilos para clientes que soporten hover (ej. Gmail web) */
                        .btn-neon:hover {
                            background-color: #3ed67e !important;
                            box-shadow: 0 0 15px rgba(76, 230, 139, 0.6) !important;
                            transform: translateY(-2px);
                        }
                    </style>
                </head>
                <body style="margin: 0; padding: 0; background-color: %s; font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: %s; padding: 40px 0;">
                        <tr>
                            <td align="center">
                
                                <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="background-color: %s; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5); max-width: 600px; width: 90%%; margin: 0 auto; border: 1px solid #1f3a30;">
                
                                    <tr>
                                        <td style="padding: 35px 0; text-align: center; background: linear-gradient(180deg, #162b24 0%%, %s 100%%);">
                                            <h1 style="margin: 0; color: %s; font-size: 36px; font-weight: 900; letter-spacing: 1px; font-style: italic;">
                                                BOOM<span style="color: %s;">BET</span>
                                            </h1>
                                        </td>
                                    </tr>
                
                                    <tr>
                                        <td style="background-color: %s; height: 2px; font-size: 0; line-height: 0;">&nbsp;</td>
                                    </tr>
                
                                    <tr>
                                        <td style="padding: 40px 40px 30px 40px; text-align: center;">
                
                                            <h2 style="color: %s; font-size: 28px; margin: 0 0 20px 0; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px;">
                                                ¡YA PODÉS FORMAR PARTE DEL CLUB! 🔥
                                            </h2>
                
                                            <p style="color: %s; font-size: 16px; line-height: 1.6; margin-bottom: 35px;">
                                                Hola <strong>%s</strong>,<br><br>
                                                Estás a un solo paso de empezar a vivir la experiencia BoomBet desde adentro. Activá tu cuenta ahora y accedé a los mejores beneficios.
                                            </p>
                
                                            <table role="presentation" cellspacing="0" cellpadding="0" style="margin: 0 auto;">
                                                <tr>
                                                    <td align="center">
                                                        <a href="%s" target="_blank" class="btn-neon" style="display: inline-block; padding: 14px 40px; background-color: %s; color: #09120f; font-size: 16px; font-weight: 800; text-decoration: none; border-radius: 50px; text-transform: uppercase; letter-spacing: 1px; box-shadow: 0 4px 15px rgba(76, 230, 139, 0.3); transition: all 0.3s ease;">
                                                            VERIFICAR MI CUENTA
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                
                                            <p style="color: %s; font-size: 13px; margin-top: 40px; margin-bottom: 10px;">
                                                Si el botón no funciona, copiá y pegá este enlace:
                                            </p>
                                            <p style="margin: 0; word-break: break-all; background-color: #0a1612; padding: 10px; border-radius: 5px; border: 1px solid #1f3a30;">
                                                <a href="%s" style="color: %s; text-decoration: none; font-size: 12px;">
                                                    %s
                                                </a>
                                            </p>
                                        </td>
                                    </tr>
                
                                    <tr>
                                        <td style="background-color: #0a1612; padding: 25px; text-align: center; border-top: 1px solid #1f3a30;">
                                            <p style="color: #5c7a6f; font-size: 11px; margin: 0; text-transform: uppercase; font-weight: bold; letter-spacing: 1px;">
                                                © 2025 BOOMBET ARGENTINA
                                            </p>
                                            <p style="color: #4a635a; font-size: 11px; margin: 10px 0 0 0;">
                                                Jugar compulsivamente es perjudicial para la salud. +18.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                darkBgMain, darkBgMain, darkBgCard, darkBgCard, textColorPrimary, verdeNeon, // Colores del head/body/card/logo
                verdeNeon, // Color del separador
                textColorPrimary, textColorSecondary, nombre, // Título y texto cuerpo
                link, verdeNeon, // Link del botón y color del botón
                textColorSecondary, link, verdeNeon, link // Link alternativo y sus colores
        );
    }

    public static String construirEmailRecuperacion(String nombre, String link) {
        // Mismos colores de marca que el de bienvenida
        String verdeNeon = "#4CE68B";
        String darkBgMain = "#09120f";
        String darkBgCard = "#11211c";
        String textColorPrimary = "#ffffff";
        String textColorSecondary = "#b7c4bd";

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Recuperar Contraseña</title>
            <style>
                .btn-neon:hover {
                    background-color: #3ed67e !important;
                    box-shadow: 0 0 15px rgba(76, 230, 139, 0.6) !important;
                }
            </style>
        </head>
        <body style="margin: 0; padding: 0; background-color: %s; font-family: 'Segoe UI', sans-serif;">
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: %s; padding: 40px 0;">
                <tr>
                    <td align="center">
                        <table role="presentation" width="600" style="background-color: %s; border-radius: 12px; max-width: 600px; width: 90%%; border: 1px solid #1f3a30; box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
                            <tr>
                                <td style="padding: 30px 0; text-align: center; border-bottom: 2px solid %s;">
                                    <h1 style="margin: 0; color: %s; font-size: 32px; font-style: italic;">
                                        BOOM<span style="color: %s;">BET</span>
                                    </h1>
                                </td>
                            </tr>
                            <tr>
                                <td style="padding: 40px 40px 30px 40px; text-align: center;">
                                    <h2 style="color: %s; font-size: 24px; margin-bottom: 20px; text-transform: uppercase;">
                                        ¿OLVIDASTE TU CONTRASEÑA? 🔑
                                    </h2>
                                    <p style="color: %s; font-size: 16px; line-height: 1.6; margin-bottom: 30px;">
                                        Hola <strong>%s</strong>,<br>
                                        No te preocupes, es algo que pasa. Hacé clic en el botón de abajo para crear una nueva contraseña y volver al juego.
                                    </p>
                                    
                                    <a href="%s" class="btn-neon" style="display: inline-block; padding: 14px 30px; background-color: %s; color: #09120f; font-weight: 800; text-decoration: none; border-radius: 50px; text-transform: uppercase;">
                                        RESTABLECER CONTRASEÑA
                                    </a>

                                    <p style="color: %s; font-size: 13px; margin-top: 40px; margin-bottom: 10px;">
                                            Si el botón no funciona, copiá y pegá este enlace:
                                        </p>
                                        <p style="margin: 0; word-break: break-all; background-color: #0a1612; padding: 10px; border-radius: 5px; border: 1px solid #1f3a30;">
                                            <a href="%s" style="color: %s; text-decoration: none; font-size: 12px;">
                                                %s
                                            </a>
                                        </p>
                                    <p style="color: %s; font-size: 13px; margin-top: 30px;">
                                        Si no solicitaste este cambio, simplemente ignorá este correo. Tu cuenta está segura.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(
                darkBgMain, darkBgMain, darkBgCard, verdeNeon, // 1-4: Estructura y bordes
                textColorPrimary, verdeNeon,                   // 5-6: Logo
                textColorPrimary, textColorSecondary, nombre,  // 7-9: Títulos y Saludo
                link, verdeNeon,                               // 10-11: Botón principal
                textColorSecondary,                            // 12: Texto "Si el botón no funciona..."
                link, verdeNeon, link,                         // 13-15: Link manual (href, color, texto)
                textColorSecondary                             // 16: Disclaimer final
        );
    }
}
