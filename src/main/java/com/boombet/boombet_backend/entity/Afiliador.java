package com.boombet.boombet_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="afiliadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Afiliador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(name="id_chat")
    private Long idChat;

    @Column(name="cant_afiliaciones", nullable = false)
    private Integer cantAfiliaciones = 0;

    @Column(name="token_afiliador")
    private String tokenAfiliador;

    private boolean activo;
    private String email;
    private String dni;
    private String telefono;

    //Faltan los EVENTOS.
}
