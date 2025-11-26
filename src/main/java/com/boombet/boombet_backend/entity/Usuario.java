package com.boombet.boombet_backend.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class Usuario implements UserDetails {
    public enum Role{
        USER,
        ADMIN
    }

    public enum Status{
        PENDING,
        AFFILIATED
    }


    //Falta relacionar el usuario con un jugador.
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
    private String dni;

    private Status status;

    @NotNull
    @Column(nullable=false)
    private Character genero;

    @NotNull
    @Column(nullable=false)
    private String telefono;


    @Enumerated(EnumType.STRING)
    Role role;

    @NotNull
    @Column(nullable=false)
    private String username;



    //JWT
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
