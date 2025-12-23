package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.JugadorRepository;
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
import java.util.Optional;
import java.util.UUID;

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

    private final EmailService emailService;
    private final JdbcTemplate jdbcTemplate;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final JugadorRepository jugadorRepository;
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
            EmailService emailService, JugadorRepository jugadorRepository
    ) {
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
    }



    @Transactional
    public void register(RegistroRequestDTO inputWrapper) {
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

        if (!usuario.isVerified()) {
            throw new RuntimeException("Usuario no verificado");
        }

        String token = jwtService.getToken(usuario);

        return AuthResponseDTO.builder()
                .token(token)
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
                usuario.getEmail()
        );
    }

    @Transactional
    public void desafiliar(Long idUsuario){ //podría ser un soft delete?
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


}