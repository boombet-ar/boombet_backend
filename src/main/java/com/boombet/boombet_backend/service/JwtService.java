package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    /*public String getToken(UserDetails usuario){
        return getToken(new HashMap<>(),usuario);
    }
    */


    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60;

    // Duración: 7 Días para Refresh Token
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;


    public String generateAccessToken(UserDetails usuario) {
        return buildToken(new HashMap<>(), usuario, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(UserDetails usuario) {
        return buildToken(new HashMap<>(), usuario, REFRESH_TOKEN_EXPIRATION);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        Usuario user = (Usuario) userDetails;
        Long jugadorId = (user.getJugador() != null) ? user.getJugador().getId() : null;

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .claim("idJugador", jugadorId)
                .claim("role",user.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    /**
     * Crea el JWT para un usuario.
     * @param extraClaims Data extra que lleva el JWT.
     * @param userDetails
     * @return
     */
    /*
    private String getToken(Map<String,Object> extraClaims, UserDetails userDetails) {

        Usuario user = (Usuario) userDetails;

        Long jugadorId = (user.getJugador() != null) ? user.getJugador().getId() : null;
        String email = user.getEmail();


        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .claim("idJugador", jugadorId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000*60*60*24))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    */


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getKey() {
        byte[] keyBytes= Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        // Extraemos el email que viene en el token (aunque el método se llame extractUsername)
        final String emailInToken = extractUsername(token);

        // Obtenemos el email real del usuario de la DB
        // Hacemos cast porque UserDetails no tiene getEmail() nativo
        String userEmail = ((Usuario) userDetails).getEmail();

        return (emailInToken.equals(userEmail)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
