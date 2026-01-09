package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.controller.FCMController;
import com.boombet.boombet_backend.dao.JugadorRepository;
import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.dto.*;
import com.boombet.boombet_backend.entity.Jugador;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.utils.UsuarioUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class UsuarioService {

    @Value("${front.verifyurl}")
    String frontVerifyUrl;

    @Value("${front.passwordurl}")
    String frontPasswordUrl;

    @Value("${affiliator.api.key}")
    private String affiliatorToken;



    @Autowired
    @Lazy
    private UsuarioService self;
    private final ObjectMapper objectMapper;
    private final FCMService fcmService;
    private final JugadorRepository jugadorRepository;
    private final AzureBlobService azureBlobService;
    private final EmailService emailService;
    private final JdbcTemplate jdbcTemplate;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final RestClient restClient;
    private final JugadorService jugadorService;
    private final WebSocketService websocketService;


    public UsuarioService(
            ObjectMapper objectMapper, JdbcTemplate jdbcTemplate,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            UsuarioRepository usuarioRepository,
            AuthenticationManager authenticationManager,
            JugadorService jugadorService,
            WebSocketService websocketService,
            @Qualifier("affiliatorRestClient") RestClient restClient,
            EmailService emailService,
            JugadorRepository jugadorRepository,
            AzureBlobService azureBlobService,
            FCMService fcmService

    ) {
        this.objectMapper = objectMapper;
        this.jugadorService = jugadorService;
        this.jdbcTemplate = jdbcTemplate;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.restClient = restClient;
        this.websocketService = websocketService;
        this.emailService = emailService;
        this.jugadorRepository = jugadorRepository;
        this.azureBlobService = azureBlobService;
        this.fcmService = fcmService;
    }



    @Transactional
    public AuthResponseDTO register(RegistroRequestDTO inputWrapper) {
        /*
         * Hashea la contraseña
         * Hace la solicitud a datadash y recibe datos del usuario
         * Crea un jugador y lo vincula con el usuario
         * */


        AffiliationDTO userData = inputWrapper.getConfirmedData();

        //Si ya habia un usuario sin verificar, lo pisa
        //Si habia un usuario verificado, error
        //Si no habia nada, lo crea

        UsuarioUtils.validarFormatoPassword(userData.getPassword());

        if(usuarioRepository.existsByUsername(userData.getUser())){
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre");
        }

        String hashedPass = passwordEncoder.encode(userData.getPassword());

        Usuario nuevoUsuario = usuarioRepository.findByEmail(userData.getEmail())
                .or(() -> usuarioRepository.findByDni(userData.getDni()))
                .orElse(new Usuario());


        Jugador jugador = jugadorRepository.findByEmail(userData.getEmail())
                .or(() -> jugadorRepository.findByDni(userData.getDni()))
                .orElseGet( () -> jugadorService.crearJugador(userData));


        if (nuevoUsuario.getId() != null && nuevoUsuario.isVerified()) {
            throw new IllegalArgumentException("Ya existe una cuenta verificada con este correo o DNI");
        }

        nuevoUsuario.setUsername(userData.getUser());
        nuevoUsuario.setPassword(hashedPass);
        nuevoUsuario.setRole(Usuario.Role.USER);
        nuevoUsuario.setDni(userData.getDni());
        nuevoUsuario.setEmail(userData.getEmail());
        Usuario.Genero generoEnum = Usuario.Genero.fromString(userData.getGenero());
        nuevoUsuario.setGenero(generoEnum);
        nuevoUsuario.setTelefono(userData.getTelefono());
        nuevoUsuario.setJugador(jugador);

        String verificationToken = UUID.randomUUID().toString();
        nuevoUsuario.setVerificationToken(verificationToken);
        nuevoUsuario.setVerified(false);

        usuarioRepository.save(nuevoUsuario);

        String htmlBody;

        if (inputWrapper.getN8nWebhookLink() != null){ //Logica para determinar si ingreso por form o por app
            htmlBody = UsuarioUtils.construirEmailBienvenida(userData.getNombre(), inputWrapper.getN8nWebhookLink() + verificationToken);
        }else {
            String verificacionLink = frontVerifyUrl + verificationToken;
            htmlBody = UsuarioUtils.construirEmailBienvenida(userData.getNombre(), verificacionLink);
        }

        emailService.enviarCorreo(
                nuevoUsuario.getEmail(),
                "Verifica tu cuenta en Boombet",
                htmlBody
        );


        String token = jwtService.getToken(nuevoUsuario);

        return AuthResponseDTO.builder()
                .token(token)
                .playerData(userData)
                .build();
    }

    @Async
    public void iniciarAfiliacionAsync(AffiliationDTO datosAfiliacion, String websocketLink) {
        System.out.println("---- INICIANDO AFILIACIÓN EN HILO ASÍNCRONO ----");
        System.out.println("CUIT Enviado: " + datosAfiliacion.getCuit());

        try {
            String nombreProvincia = datosAfiliacion.getProvincia();

            if (nombreProvincia == null) {
                System.err.println("Afiliación fallida: No hay provincia en los datos.");
                return;
            }


            String query = "SELECT alias FROM provincias WHERE nombre = ?";
            String provinciaAlias;
            try {
                provinciaAlias = jdbcTemplate.queryForObject(query, String.class, nombreProvincia);
            } catch (Exception e) {
                System.err.println("Provincia no encontrada en DB de alias: " + nombreProvincia);
                enviarErrorPorSocket(websocketLink, "Error: La provincia '" + nombreProvincia + "' no es válida o no existe.");
                return;
            }


            Map<String, Object> respuestaApi = restClient.post()
                    .uri("/register/" + provinciaAlias)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(datosAfiliacion)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });


            System.out.println("---- RESPUESTA RECIBIDA, NOTIFICANDO WEBSOCKET ----");
            WebsocketDTO notificacion = new WebsocketDTO();
            notificacion.setWebsocketLink(websocketLink); // Para que el servicio extraiga el ID

            if (respuestaApi != null) {
                // 1. Extraer playerData si existe en la respuesta externa
                if (respuestaApi.containsKey("playerData") && respuestaApi.get("playerData") instanceof Map) {
                    notificacion.setPlayerData((Map<String, Object>) respuestaApi.get("playerData"));
                } else {
                    // Fallback: si no viene, podríamos mandar vacío o los datos originales
                    notificacion.setPlayerData(new HashMap<>());
                }

                // 2. Extraer responses (los resultados de los casinos) si existe
                if (respuestaApi.containsKey("responses") && respuestaApi.get("responses") instanceof Map) {
                    notificacion.setResponses((Map<String, Object>) respuestaApi.get("responses"));
                } else {
                    notificacion.setResponses(new HashMap<>());
                }
            }
            websocketService.sendToWebSocket(notificacion);
            usuarioRepository.findByEmail(datosAfiliacion.getEmail()).ifPresent(usuario -> {
                try {
                    System.out.println("---- PREPARANDO NOTIFICACIÓN FCM ----");

                    // 2. Preparamos el mapa de datos (DATA PAYLOAD)
                    // Recordá: FCM solo acepta <String, String> en el campo 'data'
                    Map<String, String> dataFCM = new HashMap<>();

                    // A. Agregamos el Deeplink directo
                    dataFCM.put("\"deeplink\"", "boombet://affiliation/completed");

                    // Convertimos la respuesta compleja de la API a un String JSON
                    // Esto permite que el objeto viaje "empaquetado" dentro del mapa de strings
                    String jsonRespuesta = objectMapper.writeValueAsString(respuestaApi);
                    dataFCM.put("payload_json", jsonRespuesta);

                    NotificacionDTO.NotificacionRequestDTO notifRequest = NotificacionDTO.NotificacionRequestDTO.builder()
                            .title("¡Afiliación Completada!")
                            .body("Presiona para ver el estado de tus afiliaciones!")
                            .data(dataFCM)
                            .build();

                    fcmService.sendNotificationToUser(notifRequest, usuario.getId());

                    System.out.println("✅ Notificación FCM enviada al usuario ID: " + usuario.getId());

                } catch (Exception e) {
                    System.err.println("❌ Error enviando FCM en afiliación: " + e.getMessage());
                    e.printStackTrace();
                    enviarErrorPorSocket(websocketLink, e.getMessage());
                }
            });
            System.out.println("---- AFILIACIÓN COMPLETADA EXITOSAMENTE (" + provinciaAlias + ") ----");
        } catch (Exception e) {
            System.err.println("Error en afiliación: " + e.getMessage());

            enviarErrorPorSocket(websocketLink, e.getMessage());
        }
    }

    private void enviarErrorPorSocket(String link, String errorMsg) {
        WebsocketDTO errorDto = new WebsocketDTO();
        errorDto.setWebsocketLink(link);
        Map<String, Object> errMap = new HashMap<>();
        errMap.put("error", errorMsg);
        errorDto.setResponses(errMap);
        websocketService.sendToWebSocket(errorDto);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                )
        );

        Usuario usuario = usuarioRepository.findByUsernameOrEmail(request.getIdentifier(), request.getIdentifier())
                .orElseThrow();

        String fcmToken = null;
        if (request.getFcmToken() != null && !request.getFcmToken().isEmpty()) {
            fcmToken = request.getFcmToken();
            usuario.setFcmToken(fcmToken);
            usuarioRepository.save(usuario);
        }

        if (!usuario.isVerified()) {
            throw new RuntimeException("Usuario no verificado");
        }

        String token = jwtService.getToken(usuario);

        return AuthResponseDTO.builder()
                .token(token)
                .fcmToken(fcmToken)
                .build();
    }

    public void verificarUsuario(String token) {

        Usuario usuario = usuarioRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("El link de verificación es inválido o ya fue utilizado."));

        usuario.setVerified(true);
        usuario.setVerificationToken(null);
        usuarioRepository.save(usuario);
    }

    public void solicitarCambioDeContraseña(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario registrado con este email."));

        String verificationToken = UUID.randomUUID().toString();

        usuario.setResetToken(verificationToken);
        usuarioRepository.save(usuario);

        String resetLink = frontPasswordUrl + verificationToken;

        String nombre = (usuario.getJugador() != null) ? usuario.getJugador().getNombre() : usuario.getUsername();

        String htmlBody = UsuarioUtils.construirEmailRecuperacion(nombre, resetLink);
        emailService.enviarCorreo(usuario.getEmail(), "Recuperá tu acceso a Boombet 🔐", htmlBody);

    }

    public void restablecerContrasena(String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("El enlace de recuperación es inválido o ya fue utilizado."));

        UsuarioUtils.validarFormatoPassword(newPassword);

        usuario.setPassword(passwordEncoder.encode(newPassword));

        usuario.setResetToken(null);

        usuarioRepository.save(usuario);
    }

    public UsuarioDTO.UsuarioResponse obtenerDatosDeUsuario(Usuario usuario) {

        return new UsuarioDTO.UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getDni(),
                //usuario.getPuntos(),
                usuario.getEmail(),
                usuario.isBondaEnabled(),
                usuario.isDeleted(),
                usuario.getIconUrl()

        );
    }

    @Transactional
    public void desafiliar(Long idUsuario){ //soft delete
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario"));

        Jugador jugador = jugadorRepository.findById(usuario.getJugador().getId())
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el jugador"));

        try{
            usuarioRepository.delete(usuario);
            jugadorRepository.delete(jugador);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }


    @Value("${spring.cloud.azure.storage.blob.container-name_iconos}")
    private String iconsContainer;

    @Transactional
    public String cambiarIcono(Long idUsuario, MultipartFile file){
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario"));

        if(usuario.getIconUrl() != null){
            try {
                azureBlobService.deleteBlob(usuario.getIconUrl(), iconsContainer);
            }catch(Exception e){
                System.err.println("Error al eliminar la imagen anterior" + e.getMessage());
            }
        }

        try {
            String blobUrl = azureBlobService.uploadFile(file, iconsContainer);


            usuario.setIconUrl(blobUrl);
            usuarioRepository.save(usuario);
            return blobUrl;
        } catch(RuntimeException e) {
            throw new RuntimeException(e);
        }
    }


    public List<CasinoDTO.casinosList> listarCasinosAfiliados(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getJugador() == null) {
            throw new RuntimeException("El usuario no tiene un perfil de jugador asociado");
        }

        Long idJugador = usuario.getJugador().getId();

        return jugadorRepository.encontrarCasinosDelJugador(idJugador);
    }

}