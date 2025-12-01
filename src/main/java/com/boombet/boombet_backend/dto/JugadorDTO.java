package com.boombet.boombet_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JugadorDTO {

    private String username;

    private Long id;

    @JsonProperty("id_jugador")
    private Long idJugador;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @Email(message = "Formato de email inválido")
    @NotBlank
    private String email;

    private String telefono;
    private String genero;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String dni;
    private String cuit;

    @JsonProperty("est_civil")
    private String estCivil;

    private String calle;

    @JsonProperty("num_calle")
    private String numCalle;

    private String provincia;
    private String ciudad;
    private String cp;
}