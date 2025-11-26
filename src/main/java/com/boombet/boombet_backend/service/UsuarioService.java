package com.boombet.boombet_backend.service;



import com.boombet.boombet_backend.dao.UsuarioRepository;

import com.boombet.boombet_backend.dto.AuthResponseDTO;
import com.boombet.boombet_backend.dto.DatadashDTO;
import com.boombet.boombet_backend.dto.LoginRequestDTO;
import com.boombet.boombet_backend.dto.RegistroRequestDTO;
import com.boombet.boombet_backend.entity.Usuario;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

    public AuthResponseDTO register(RegistroRequestDTO credsUsuario){
        String hashedPass = passwordEncoder.encode(credsUsuario.getPassword()); //Hashed password

        Usuario nuevoUsuario = new Usuario();

        Object playerData = datadashService.getUserData(credsUsuario.getDni(), credsUsuario.getGenero());

        if (usuarioRepository.existsByEmail(credsUsuario.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con este correo");
        }

        nuevoUsuario.setUsername(credsUsuario.getUsername());
        nuevoUsuario.setPassword(hashedPass);
        nuevoUsuario.setRole(Usuario.Role.USER);
        nuevoUsuario.setDni(credsUsuario.getDni());
        nuevoUsuario.setEmail(credsUsuario.getEmail());
        nuevoUsuario.setGenero(credsUsuario.getGenero());
        nuevoUsuario.setTelefono(credsUsuario.getTelefono());
        usuarioRepository.save(nuevoUsuario);

        AuthResponseDTO registerResult = AuthResponseDTO.builder()
                .token(jwtService.getToken(nuevoUsuario))
                .playerData(playerData)
                .build();

        return registerResult;
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



    @Async
    public void startAffiliate(Map<String, Object> playerData, String websocketLink){

        if (playerData == null || !playerData.containsKey("provincia")) {
            throw new IllegalArgumentException("No se encontró provincia en playerData");

        }
        /*Recibe los datos ya confirmados del jugador, y con esos datos empieza la afiliacion
        * utilizando la api de afiliaciones. devuelve 200 instantaneamente para no trabar la app. */

        try {

            String nombreProvincia = (String) playerData.get("provincia");

            String query = "SELECT alias FROM provincias WHERE nombre = ?";
            String provinciaAlias;
            try {
                provinciaAlias = jdbcTemplate.queryForObject(query, String.class, nombreProvincia);
            } catch (Exception e) {
                System.err.println("Provincia no encontrada en DB de alias: " + nombreProvincia);
                return;
            }

            Map<String, Object> requestBody = new HashMap<>();

            requestBody.put("playerData", playerData);

            requestBody.put("websocketLink", websocketLink);

            restClient.post()
                    .uri("/register/" + provinciaAlias)
                    .header("register_key", affiliatorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();
        }catch (Exception e) {
             {
                System.err.println("Error en afiliación asíncrona: " + e.getMessage());
            }
        }
    }

}