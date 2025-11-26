package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.dto.*;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.utils.UsuarioUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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
    private final DatadashService datadashService;
    private final RestClient restClient;

    public UsuarioService(
            JdbcTemplate jdbcTemplate,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            UsuarioRepository usuarioRepository,
            AuthenticationManager authenticationManager,
            DatadashService datadashService,
            @Qualifier("affiliatorRestClient") RestClient restClient
    ){
        this.jdbcTemplate = jdbcTemplate;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.datadashService = datadashService;
        this.restClient = restClient;
    }

    public AuthResponseDTO register(RegistroRequestDTO inputWrapper) {
        AffiliationDTO userData = inputWrapper.getConfirmedData();
        String websocketLink = inputWrapper.getWebsocketLink();

        if (usuarioRepository.existsByEmail(userData.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con este correo");
        }

        UserDataRequestDTO requestForDatadash = UserDataRequestDTO.builder()
                .dni(userData.getDni())
                .genero(userData.getGenero())
                .email(userData.getEmail())
                .password(userData.getPassword())
                .telefono(userData.getTelefono())
                .username(userData.getUser())
                .build();

        String cuitGenerado = UsuarioUtils.generarCuit(requestForDatadash);
        userData.setCuit(cuitGenerado);

        DatadashDTO.DatadashInformResponse dataDashResponse = datadashService.getUserData(requestForDatadash);

        String hashedPass = passwordEncoder.encode(userData.getPassword());
        Usuario nuevoUsuario = new Usuario();

        nuevoUsuario.setUsername(userData.getUser());
        nuevoUsuario.setPassword(hashedPass);
        nuevoUsuario.setRole(Usuario.Role.USER);
        nuevoUsuario.setDni(userData.getDni());
        nuevoUsuario.setEmail(userData.getEmail());
        nuevoUsuario.setGenero(userData.getGenero());
        nuevoUsuario.setTelefono(userData.getTelefono());

        usuarioRepository.save(nuevoUsuario);

        // 5. DISPARAR AFILIACIÓN ASÍNCRONA (Enviando los datos del usuario, no los de DataDash)
        if (websocketLink != null && !websocketLink.isEmpty()) {
            try {
                // Pasamos el DTO userData que tiene email, user, pass, cuit, etc.
                self.iniciarAfiliacionAsync(userData, websocketLink);
            } catch (Exception e) {
                System.err.println("Error al intentar iniciar la tarea asíncrona: " + e.getMessage());
            }
        }

        return AuthResponseDTO.builder()
                .token(jwtService.getToken(nuevoUsuario))
                .playerData(dataDashResponse)
                .build();
    }

    // CAMBIO: Recibe AffiliationDTO en lugar de DatosPersonales
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

            // Buscar alias de provincia
            String query = "SELECT alias FROM provincias WHERE nombre = ?";
            String provinciaAlias;
            try {
                provinciaAlias = jdbcTemplate.queryForObject(query, String.class, nombreProvincia);
            } catch (Exception e) {
                System.err.println("Provincia no encontrada en DB de alias: " + nombreProvincia);
                return;
            }

            // Construir payload
            Map<String, Object> requestBody = new HashMap<>();
            // Aquí enviamos el objeto que tiene user, password, email, nombre, apellido...
            requestBody.put("playerData", datosAfiliacion);
            requestBody.put("websocketLink", websocketLink);

            restClient.post()
                    .uri("/register/" + provinciaAlias)
                    .header("register_key", affiliatorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            System.out.println("---- AFILIACIÓN COMPLETADA EXITOSAMENTE (" + provinciaAlias + ") ----");

        } catch (Exception e) {
            System.err.println("Error en afiliación asíncrona: " + e.getMessage());
        }
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