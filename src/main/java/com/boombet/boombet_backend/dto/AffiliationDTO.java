package com.boombet.boombet_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AffiliationDTO {

    private String nombre;
    private String apellido;

    @Email
    private String email;

    private String telefono;
    private String genero;

    @JsonProperty("fecha_nacimiento")
    private String fechaNacimiento;

    private String dni;
    private String cuit;

    @JsonProperty("est_civil")
    private String estCivil;

    private String calle;
    private String numCalle;
    private String provincia;
    private String ciudad;
    private String cp;
    private String user;
    private String password;
}