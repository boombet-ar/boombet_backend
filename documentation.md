# Documentación de Flujos de la Aplicación Boombet

## Resumen General

La aplicación Boombet actúa como un backend central para la gestión de usuarios y su afiliación a diversos casinos asociados. No maneja directamente el juego ni las transacciones monetarias, sino que su función principal es registrar jugadores y orquestar su inscripción en las plataformas de casino con las que existe un convenio, a través de una API externa conocida como "Affiliator API".

**Actores Principales:**
*   **Usuario/Jugador:** El único actor del sistema. Se registra, verifica su cuenta y gestiona sus afiliaciones.

**Integraciones Externas Clave:**
*   **Affiliator API:** Microservicio responsable de procesar las afiliaciones a los casinos.
*   **Datadash:** Servicio para la extracción y validación de datos del usuario.
*   **Bonda:** Servicio para la gestión de cupones y beneficios.
*   **Servicio de Email:** Para enviar correos de verificación y recuperación de contraseña.
*   **Azure Blob Storage:** Para almacenar imágenes de perfil de usuario (iconos).
*   **Firebase Cloud Messaging (FCM):** Para enviar notificaciones push a los dispositivos de los usuarios.
*   **WebSockets:** Para comunicación en tiempo real con el cliente durante el proceso de afiliación.

---

## Flujo Principal: Registro y Afiliación de Usuario/model

El proceso más importante de la aplicación es la incorporación de un nuevo jugador. Este flujo se divide en dos fases coordinadas pero técnicamente separadas:

1.  **Fase 1: Registro Sincrónico del Usuario.**
2.  **Fase 2: Afiliación Asíncrona a los Casinos.**

### Fase 1: Registro de Usuario

Esta fase se encarga de crear la cuenta del usuario en el sistema de Boombet y verificar su identidad por correo electrónico.

**Diagrama de Secuencia (Simplificado):**
```
Cliente -> UsuarioController -> UsuarioService -> Repositories & EmailService -> Cliente
```

**Pasos Detallados:**

1.  **Inicio del Registro:**
    *   El usuario completa el formulario de registro en la aplicación cliente.
    *   El cliente envía una petición `POST` al endpoint: `/api/users/auth/register`.
    *   El cuerpo de la petición (`RegistroRequestDTO`) contiene todos los datos del usuario.

2.  **Controlador (`UsuarioController`):**
    *   El método `register()` recibe la petición.
    *   Delega inmediatamente toda la lógica de negocio al método `register()` en `UsuarioService`.

3.  **Servicio (`UsuarioService`):**
    *   **Validación de Datos:** Se realizan varias comprobaciones:
        *   Se valida el formato de la contraseña.
        *   Se verifica que el nombre de usuario no exista previamente.
        *   Se comprueba si ya existe una cuenta **verificada** con el mismo email o DNI. Si es así, se lanza un error.
    *   **Creación/Actualización de Entidades:**
        *   El sistema busca si ya existe un usuario (por email o DNI) que quizás no completó la verificación. Si existe, lo actualiza; si no, crea una nueva instancia de la entidad `Usuario`.
        *   De forma similar, crea una entidad `Jugador` asociada, que contendrá datos específicos del perfil del jugador.
        *   Se hashea la contraseña del usuario para almacenarla de forma segura.
    *   **Token de Verificación:**
        *   Se genera un token de verificación único (UUID).
        *   La cuenta se marca como `isVerified = false` y se le asigna el token.
    *   **Persistencia:** Se guarda el nuevo usuario (y su jugador asociado) en la base de datos (`usuarioRepository.save()`).
    *   **Envío de Correo:**
        *   Se construye un enlace de verificación único.
        *   Se utiliza el `EmailService` para enviar un correo de bienvenida al usuario, pidiéndole que haga clic en el enlace para verificar su cuenta.
    *   **Respuesta al Cliente:**
        *   Se genera un JSON Web Token (JWT) para el nuevo usuario, permitiéndole quedar autenticado en la app.
        *   Se devuelve una respuesta (`AuthResponseDTO`) al cliente con el `accessToken`.

---

### Fase 2: Afiliación a Casinos

Una vez que el usuario está registrado, la aplicación cliente inicia el proceso de afiliación. Este proceso es asíncrono para no hacer esperar al usuario, ya que puede implicar la comunicación con múltiples sistemas externos.

**Diagrama de Secuencia (Simplificado):**
```
Cliente -> UsuarioController -> UsuarioService --(Async)--> Affiliator API
                                                     |
                                                     +--> WebSocketService -> Cliente
                                                     |
                                                     +--> FCMService -> Dispositivo del Usuario
```

**Pasos Detallados:**

1.  **Inicio de la Afiliación:**
    *   El cliente envía una petición `POST` al endpoint: `/api/users/auth/affiliate`.
    *   El cuerpo de la petición vuelve a ser el `RegistroRequestDTO`.

2.  **Controlador (`UsuarioController`):**
    *   El método `affiliate()` recibe la petición.
    *   Llama al método `iniciarAfiliacionAsync()` en `UsuarioService`. Este método está anotado con `@Async`, por lo que se ejecuta en un hilo separado.
    *   El controlador responde inmediatamente al cliente con un `HTTP 200 OK`, indicando que la afiliación ha comenzado.

3.  **Servicio (`UsuarioService` - Hilo Asíncrono):**
    *   **Llamada a la API de Afiliación:**
        *   Se obtiene el alias de la provincia del usuario desde la base de datos (ej. "Buenos Aires" -> "BA").
        *   Se realiza una petición `POST` a la **Affiliator API** (`[URL]/register/[provinciaAlias]`), enviando los datos del jugador.
    *   **Manejo de la Respuesta:**
        *   La Affiliator API devuelve un objeto JSON que contiene los resultados de la afiliación en cada uno de los casinos disponibles para esa provincia.
    *   **Notificación al Cliente (Tiempo Real):**
        *   Se utiliza el `WebSocketService` para enviar la respuesta completa de la Affiliator API directamente al cliente a través de una conexión WebSocket establecida previamente. Esto permite que la interfaz de usuario se actualice en tiempo real mostrando el estado de cada afiliación.
    *   **Notificación Push:**
        *   Se busca el token de dispositivo del usuario.
        *   Se construye una notificación push usando `FCMService` (Firebase Cloud Messaging).
        *   Se envía la notificación al dispositivo del usuario para informarle que el proceso ha finalizado, incluso si la app está en segundo plano. La notificación incluye un "deeplink" para llevar al usuario directamente a la pantalla de resultados.
    *   **Manejo de Errores:** Si ocurre algún error durante la llamada a la Affiliator API, el error se captura y se envía al cliente a través de la misma conexión WebSocket.
