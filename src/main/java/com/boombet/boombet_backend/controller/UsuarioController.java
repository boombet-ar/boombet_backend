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
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<Void> register(@Valid @RequestBody RegistroRequestDTO credsUsuario) {
        try {
            usuarioService.register(credsUsuario);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO credsUsuario) {
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


    @PostMapping("/auth/affiliate") //Cuando el usuario le pega a /verify, debe pegarle tambien a este endpoint.
    public ResponseEntity<String> affiliate(@RequestBody RegistroRequestDTO input) {
        try {
            usuarioService.iniciarAfiliacionAsync(input.getConfirmedData(), input.getWebsocketLink());
            return ResponseEntity.ok("Afiliación iniciada.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
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
}
