package com.boombet.boombet_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name="casinos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Casino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre_casino")
    private String nombreCasino;

    private String url;

    @Column(name="casino_gral_id")
    private Long casinoGralId;

    @Column(name="provincia_id")
    private Long provinciaId;

    @Column(name="logo_url")
    private String logoUrl;

}
