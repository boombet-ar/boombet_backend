package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.dto.*;
import com.boombet.boombet_backend.entity.Jugador;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.utils.UsuarioUtils;

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

import java.util.HashMap;
import java.util.Map;

@Service
public class UsuarioService {

    @Value("${front.verifyurl}")
    String frontVerifyUrl;

    @Value("${affiliator.api.key}")
    private String affiliatorToken;

    @Autowired
    @Lazy
    private UsuarioService self;

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
            JdbcTemplate jdbcTemplate,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            UsuarioRepository usuarioRepository,
            AuthenticationManager authenticationManager,
            JugadorService jugadorService,
            WebSocketService websocketService,
            @Qualifier("affiliatorRestClient") RestClient restClient,
            EmailService emailService
    ){
        this.jugadorService = jugadorService;
        this.jdbcTemplate = jdbcTemplate;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.restClient = restClient;
        this.websocketService = websocketService;
        this.emailService = emailService;
    }




    @Transactional
    public void register(RegistroRequestDTO inputWrapper) {
        /*
         * Hashea la contraseña
         * Hace la solicitud a datadash y recibe datos del usuario
         * Crea un jugador y lo vincula con el usuario
         * */

        AffiliationDTO userData = inputWrapper.getConfirmedData();


        UsuarioUtils.validarFormatoPassword(userData.getPassword());
        if (usuarioRepository.existsByEmail(userData.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con este correo");
        }

        Jugador jugador = jugadorService.crearJugador(userData);

        String hashedPass = passwordEncoder.encode(userData.getPassword());
        Usuario nuevoUsuario = new Usuario();

        nuevoUsuario.setUsername(userData.getUser());
        nuevoUsuario.setPassword(hashedPass);
        nuevoUsuario.setRole(Usuario.Role.USER);
        nuevoUsuario.setDni(userData.getDni());
        nuevoUsuario.setEmail(userData.getEmail());
        Usuario.Genero generoEnum = Usuario.Genero.fromString(userData.getGenero());
        nuevoUsuario.setGenero(generoEnum);
        nuevoUsuario.setTelefono(userData.getTelefono());
        nuevoUsuario.setJugador(jugador);

        String verificationToken = jwtService.getToken(nuevoUsuario);
        nuevoUsuario.setVerificationToken(verificationToken);
        nuevoUsuario.setVerified(false);

        usuarioRepository.save(nuevoUsuario);


        String verificacionLink = frontVerifyUrl + verificationToken;
        //url hardcodeada, arreglar. la url debe ser la del frontend. en esa ruta, el frontend debe pegarle a
        // /api/users/auth/verify

        String htmlBody = UsuarioUtils.construirEmailBienvenida(userData.getNombre(), verificacionLink);

        emailService.enviarCorreo(
                nuevoUsuario.getEmail(),
                "Verifica tu cuenta en Boombet",
                htmlBody
        );

        //Solo devuelve un 200 si funcionó.

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



            System.out.println(websocketLink);

            Map<String, Object> respuestaApi = restClient.post()
                    .uri("/register/" + provinciaAlias)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(datosAfiliacion)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
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

        if (!usuario.isVerified()){
            throw new RuntimeException("Usuario no verificado");
        }

        String token = jwtService.getToken(usuario);

        return AuthResponseDTO.builder()
                .token(token)
                .build();
    }

    public void verificarUsuario(String token) {
        String email = jwtService.extractUsername(token); //username en realidad es el mail

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (jwtService.isTokenValid(token, usuario)) {
            usuario.setVerified(true);
            usuario.setVerificationToken(null);
            usuarioRepository.save(usuario);
        } else {
            throw new IllegalArgumentException("El link de verificación ha expirado o es inválido");
        }
    }

    public void solicitarCambioDeContraseña(String email){
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario registrado con este email."));

        String token = jwtService.getToken(usuario);

        usuario.setResetToken(token);
        usuarioRepository.save(usuario);

        String frontendUrl = "http://localhost:7070/api/users/auth"; //Cambiar

        String resetLink = frontendUrl + "/recuperar-password?token=" + token;

        String nombre = (usuario.getJugador() != null) ? usuario.getJugador().getNombre() : usuario.getUsername();

        String htmlBody = UsuarioUtils.construirEmailRecuperacion(nombre, resetLink);
        emailService.enviarCorreo(usuario.getEmail(), "Recuperá tu acceso a Boombet 🔐", htmlBody);

    }


    public void restablecerContrasena(String token, String newPassword) {
        String email = jwtService.extractUsername(token);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getResetToken() == null || !usuario.getResetToken().equals(token)) {
            throw new IllegalArgumentException("El token es inválido o ya fue utilizado.");
        }

        if (!jwtService.isTokenValid(token, usuario)) {
            throw new IllegalArgumentException("El enlace ha expirado. Solicitá uno nuevo.");
        }

        UsuarioUtils.validarFormatoPassword(newPassword);

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setResetToken(null);
        usuarioRepository.save(usuario);
    }
}