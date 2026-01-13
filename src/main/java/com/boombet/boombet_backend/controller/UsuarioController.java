package com.boombet.boombet_backend.controller;
import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.dto.*;

import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.service.DatadashService;
import com.boombet.boombet_backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private DatadashService datadashService;
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService, DatadashService datadashService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.datadashService = datadashService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<AuthDTO.AuthResponseDTO> register(@Valid @RequestBody RegistroRequestDTO credsUsuario) {
        try {
            AuthDTO.AuthResponseDTO response = usuarioService.register(credsUsuario);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthDTO.AuthResponseDTO> login(@RequestBody LoginRequestDTO credsUsuario) {
        try {
            return ResponseEntity.ok(usuarioService.login(credsUsuario));
        }catch(org.springframework.security.authentication.BadCredentialsException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", e);
        }
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    @PostMapping("/auth/userData")
    public ResponseEntity<DatadashDTO.DatadashInformResponse> getUserData(@RequestBody UserDataRequestDTO input) {
        try {

            var response = datadashService.getUserData(input);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (RuntimeException e) {

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }


    @GetMapping("/auth/verify") //Verifica
    public ResponseEntity<String> verifyAccount(@RequestParam String token) {
        try {
            usuarioService.verificarUsuario(token);
            return ResponseEntity.ok("¡Cuenta verificada con éxito! Ya puedes iniciar sesión.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }


    public record ForgotPasswordDto(String email) {} //pasar a usuarioutils.dto
    public record ResetPasswordDto(String token, String newPassword){} //pasar a usuarioutils.dto

    @PostMapping("/auth/forgot-password")
    //Solicita cambio de contraseña
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordDto request) {
        try {
            usuarioService.solicitarCambioDeContraseña(request.email());
            return ResponseEntity.ok("Si el correo existe, recibirás un enlace de recuperación.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PostMapping("/auth/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDto input) {
        try {
            usuarioService.restablecerContrasena(input.token(), input.newPassword());
            return ResponseEntity.ok("Contraseña actualizada con éxito.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }


    @PostMapping("/auth/affiliate")
    public ResponseEntity<String> affiliate(@RequestBody RegistroRequestDTO input) {

        /*
        if (input.getConfirmedData() == null) {
            return ResponseEntity.badRequest().body("Faltan datos del jugador.");
        }
    */
        if(usuarioService.canAffiliate(input.getConfirmedData().getDni())){
            try {
                usuarioService.iniciarAfiliacionAsync(input.getConfirmedData(), input.getWebsocketLink());
                return ResponseEntity.ok("Afiliación iniciada.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
            }}
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No contamos con casinos disponibles en la provincia.");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO.UsuarioResponse> obtenerDatosUsuarioActual(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UsuarioDTO.UsuarioResponse response = usuarioService.obtenerDatosDeUsuario(usuario);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> eliminarCuenta(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        try {
            usuarioService.desafiliar(usuario.getId());
            return ResponseEntity.ok("Cuenta eliminada exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la cuenta: " + e.getMessage());
        }
    }

    @GetMapping("/auth/isVerified")
    public ResponseEntity<Boolean> isVerified(@RequestParam String email) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No se encontró el usuario con email: " + email));

        Boolean isVerified = usuario.isVerified();
        return ResponseEntity.ok(isVerified);
    }

    @PostMapping("set_icon")
    public ResponseEntity<Map<String, String>> setIcon(@AuthenticationPrincipal Usuario usuario,
                                                       @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "El archivo está vacío"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "El archivo debe ser una imagen"));
        }

        try {
            String url = usuarioService.cambiarIcono(usuario.getId(), file);
            return ResponseEntity.ok(Collections.singletonMap("url", url));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error interno: " + e.getMessage()));
        }
    }


    @GetMapping("/casinos_afiliados")
    public ResponseEntity<List<CasinoDTO.casinosList>> listarCasinos(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.listarCasinosAfiliados(usuario.getId()));
    }



    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthDTO.AuthResponseDTO> refreshToken(@RequestBody AuthDTO.RefreshTokenRequestDTO request) {
        try {
            return ResponseEntity.ok(usuarioService.refreshToken(request.refreshToken()));
        } catch (Exception e) {
            // Si falla, el front debe desloguear al usuario
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token inválido, inicie sesión nuevamente.");
        }
    }

}
