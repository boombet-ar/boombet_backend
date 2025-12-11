package com.boombet.boombet_backend.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name="usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder


@SQLDelete(sql = "UPDATE usuarios SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Usuario implements UserDetails {
    public enum Role{
        USER,
        ADMIN
    }

    public enum Genero {
        Masculino,
        Femenino;

        private boolean deleted = Boolean.FALSE;

        public static Genero fromString(String text) {
            for (Genero g : Genero.values()) {
                if (g.name().equalsIgnoreCase(text)) {
                    return g;
                }
            }
            throw new IllegalArgumentException("Género no soportado: " + text);
        }
    }

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "is_verified")
    private boolean isVerified;

    @Column(name = "reset_token")
    private String resetToken;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull
    @Column(nullable=false)
    private String password; //hash

    @NotNull
    @Column(nullable=false, unique = true)
    @Size(min = 7, max= 8)
    private String dni;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable=false)
    Genero genero;

    @NotNull
    @Column(nullable=false)
    private String telefono;

    //@Column(nullable=true, columnDefinition = "integer default 0")
    //private Integer puntos;

    @Enumerated(EnumType.STRING)
    Role role;

    @NotNull
    @Column(nullable=false)
    private String username;


    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jugador", referencedColumnName = "id")
    private Jugador jugador;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority((role.name())));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
