// src/main/java/com/boombet/boombet_backend/entity/Publicidad.java
package com.boombet.boombet_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "publicidades")
@Data
public class Publicidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer casinoGralId;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "mediaurl")
    private String mediaUrl;

    private String text;


}