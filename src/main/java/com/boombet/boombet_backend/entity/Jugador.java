package com.boombet.boombet_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name="jugadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "jugador")
    private Usuario usuario;

    private String nombre;

    private String apellido;

    private String email;

    private String telefono;

    private String genero;


    private LocalDate fecha_nacimiento;

    private String dni;

    private String cuit;

    private String est_civil;

    private String calle;

    private String numcalle;

    private String provincia;

    private String ciudad;

    private String cp;

}
