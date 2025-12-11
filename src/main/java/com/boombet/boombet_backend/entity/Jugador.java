package com.boombet.boombet_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name="jugadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE players SET status = 'DELETED' WHERE id = ?")
@SQLRestriction("status <> 'DELETED'")
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

    @Enumerated(EnumType.STRING)
    Usuario.Genero genero;

    private LocalDate fecha_nacimiento;

    @Size(min = 7, max= 8)
    private String dni;

    private String cuit;

    private String est_civil;

    private String calle;

    private String numcalle;

    private String provincia;

    private String ciudad;

    private String cp;

}
