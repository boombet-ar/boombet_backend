package com.boombet.boombet_backend.controller;
import com.boombet.boombet_backend.dto.*;

import com.boombet.boombet_backend.service.DatadashService;
import com.boombet.boombet_backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

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
    public ResponseEntity<DatadashDTO.DatadashInformResponse> getUserData(@RequestBody UserDataDTO input) {
        try {

            var response = datadashService.getUserData(input);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (RuntimeException e) {

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }


    @PostMapping("/startAffiliate")
    public ResponseEntity<Void> startAffiliate(@RequestBody Map<String, Object> rootPayload) {

        Map<String, Object> playerData = (Map<String, Object>) rootPayload.get("playerData");

        String websocketLink = (String) rootPayload.get("websocketLink");

        if (playerData == null || websocketLink == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            usuarioService.startAffiliate(playerData, websocketLink);
        } catch(IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }

        return ResponseEntity.ok().build();
    }

}
