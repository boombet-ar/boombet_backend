package com.boombet.boombet_backend.security;

import com.boombet.boombet_backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor // Importante: Inyecta jwtService y userDetailsService
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String token = getTokenFromRequest(request);

        if (token == null) {
            System.out.println("DEBUG: No se encontró token en la request.");
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String usernameOrEmail = jwtService.extractUsername(token);
            System.out.println("DEBUG: Usuario extraído del token: " + usernameOrEmail);

            if (usernameOrEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 2. Ver si lo encuentra en la DB
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(usernameOrEmail);
                System.out.println("DEBUG: Usuario encontrado en DB: " + userDetails.getUsername());

                // 3. Validar token
                if (jwtService.isTokenValid(token, userDetails)) {

                    // Validar que sea un ACCESS TOKEN
                    String tokenType = jwtService.extractTokenType(token);
                    if (!"ACCESS".equals(tokenType)) {
                        System.out.println("DEBUG: El token NO es un Access Token (es " + tokenType + "). Rechazando.");
                        filterChain.doFilter(request, response);
                        return;
                    }

                    System.out.println("DEBUG: Token VÁLIDO. Autenticando...");

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("DEBUG: El token es INVÁLIDO.");
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error procesando JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}