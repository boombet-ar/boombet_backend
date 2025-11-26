package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.dto.*;
import com.boombet.boombet_backend.entity.Usuario;

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

    private JdbcTemplate jdbcTemplate;
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
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.datadashService = datadashService;
        this.restClient = restClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    public AuthResponseDTO register(RegistroRequestDTO inputWrapper) {
        AffiliationDTO userData = inputWrapper.getConfirmedData();
        String websocketLink = inputWrapper.getWebsocketLink();

        if (usuarioRepository.existsByEmail(userData.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con este correo");
        }

        AffiliationDTO dataDashResponse = datadashService.getUserData(userData);

        DatadashDTO.DatosPersonales datosPersonales = null;
        if (dataDashResponse.() != null && !dataDashResponse.datos().isEmpty()) {
            datosPersonales = dataDashResponse.datos().get(0);
        }

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

        // 4. DISPARAR AFILIACIÓN ASÍNCRONA (NON-BLOCKING)
        if (datosPersonales != null && websocketLink != null && !websocketLink.isEmpty()) {
            // Usamos 'self' para invocar el método a través del proxy de Spring.
            // Esto asegura que se ejecute en un hilo separado y register retorne INMEDIATAMENTE.
            try {
                self.iniciarAfiliacionAsync(datosPersonales, websocketLink);
            } catch (Exception e) {
                System.err.println("Error al intentar iniciar la tarea asíncrona: " + e.getMessage());
            }
        }

        return AuthResponseDTO.builder()
                .token(jwtService.getToken(nuevoUsuario))
                .playerData(dataDashResponse)
                .build();
    }


    @Async
    public void iniciarAfiliacionAsync(DatadashDTO.DatosPersonales datosPersonales, String websocketLink) {
        System.out.println("---- INICIANDO AFILIACIÓN EN HILO ASÍNCRONO ----");

        try {
            String nombreProvincia = datosPersonales.provincia();

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

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("playerData", datosPersonales);
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