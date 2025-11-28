package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.dto.*;
import com.boombet.boombet_backend.entity.Jugador;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.utils.UsuarioUtils;

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

    @Value("${affiliator.api.key}")
    private String affiliatorToken;

    @Autowired
    @Lazy
    private UsuarioService self;

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
            @Qualifier("affiliatorRestClient") RestClient restClient
    ){
        this.jugadorService = jugadorService;
        this.jdbcTemplate = jdbcTemplate;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.restClient = restClient;
        this.websocketService = websocketService;
    }


    public AuthResponseDTO register(RegistroRequestDTO inputWrapper) {
        /*
         * Hashea la contraseña
         * Hace la solicitud a datadash y recibe datos del usuario
         * Crea un jugador y lo vincula con el usuario
         * Hace la solicitud a la api de afiliaciones para empezar la afiliación
         * Devuelve los datos del jugador y el link del websocket(que viene del front)
         * Con ese link de websocket se notificará al front el estado de las afiliaciones.
         * */
        AffiliationDTO userData = inputWrapper.getConfirmedData();
        String websocketLink = inputWrapper.getWebsocketLink();
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
        nuevoUsuario.setGenero(userData.getGenero());
        nuevoUsuario.setTelefono(userData.getTelefono());
        nuevoUsuario.setJugador(jugador);
        usuarioRepository.save(nuevoUsuario);

        if (websocketLink != null && !websocketLink.isEmpty()) {
            try {
                self.iniciarAfiliacionAsync(userData, websocketLink);
            } catch (Exception e) {
                System.err.println("Error al intentar iniciar la tarea asíncrona: " + e.getMessage());
            }
        }

        return AuthResponseDTO.builder()
                .token(jwtService.getToken(nuevoUsuario))
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
                return;
            }



            System.out.println(websocketLink);

            Map<String, Object> respuestaApi = restClient.post()
                    .uri("/register/" + provinciaAlias)
                    .header("register_key", affiliatorToken)
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
                    // Si la estructura externa no tiene 'responses', quizás queramos mandar todo el mapa
                    // o dejarlo vacío según tu lógica. Aquí asumimos que queremos extraerlo limpio.
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

        String token = jwtService.getToken(usuario);

        return AuthResponseDTO.builder()
                .token(token)
                .build();
    }
}