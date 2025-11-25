package com.boombet.boombet_backend.controller;
import com.boombet.boombet_backend.dto.AuthResponseDTO;
import com.boombet.boombet_backend.dto.LoginRequestDTO;
import com.boombet.boombet_backend.dto.RegistroRequestDTO;

import com.boombet.boombet_backend.dto.DatadashDTO;
import com.boombet.boombet_backend.service.DatadashService;
import com.boombet.boombet_backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private DatadashService datadashService;

    public UsuarioController(UsuarioService usuarioService, DatadashService datadashService) {
        this.usuarioService = usuarioService;
        this.datadashService = datadashService;

    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegistroRequestDTO credsUsuario) {
        try {
            return ResponseEntity.ok(usuarioService.register(credsUsuario));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/login")
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


    @PostMapping("/userData")
    public ResponseEntity<DatadashDTO.DatadashInformResponse> getUserData(@RequestBody DatadashDTO.UserDataRequest input) {
        try {

            var response = datadashService.getUserData(input.dni(), input.genero());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (RuntimeException e) {

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }


    @PostMapping("/startAffiliate")
    //En lugar de MAP<> puedo hacer un DTO que me permita validar los datos antes de pasarlos a la request
    public ResponseEntity<Void> startAffiliate(@RequestBody Map<String, Object> payload) {
        // Como el método del servicio es @Async, esta línea se ejecuta instantáneamente
        try{
            usuarioService.startAffiliate(payload);
        }catch(IllegalArgumentException e){
            throw new IllegalArgumentException(e);
        }

        return ResponseEntity.ok().build();
    }

}
