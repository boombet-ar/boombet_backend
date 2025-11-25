package com.boombet.boombet_backend.config;


import com.boombet.boombet_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {


    private final AuthenticationProvider authProvider;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http

                .csrf(csrf -> csrf.disable())

                //Endpoints publicos
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll() //libera los endpoints de autenticacion
                        .requestMatchers("/error").permitAll() //libera los endpoints de autenticacion
                        .anyRequest().authenticated()
                )
                .sessionManagement(sessionManager ->
                        sessionManager
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }
}
