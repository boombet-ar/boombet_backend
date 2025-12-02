package com.boombet.boombet_backend.controller;
import com.boombet.boombet_backend.dto.*;

import com.boombet.boombet_backend.service.DatadashService;
import com.boombet.boombet_backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private DatadashService datadashService;

    public UsuarioController(UsuarioService usuarioService, DatadashService datadashService) {
        this.usuarioService = usuarioService;
        this.datadashService = datadashService;

    }

    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegistroRequestDTO credsUsuario) {
        try {
            return ResponseEntity.ok(usuarioService.register(credsUsuario));
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


    @GetMapping("/auth/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam String token) {
        try {
            usuarioService.verificarUsuario(token);
            return ResponseEntity.ok("¡Cuenta verificada con éxito! Ya puedes iniciar sesión.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

}
