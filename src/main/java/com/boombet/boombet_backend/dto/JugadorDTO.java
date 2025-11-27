package com.boombet.boombet_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JugadorDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String genero;
    private String fecha_nacimiento;
    private String dni;
    private String cuit;
    private String est_civil;
    private String calle;
    private String numcalle;
    private String provincia;
    private String ciudad;
    private String cp;
}