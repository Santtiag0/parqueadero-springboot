package com.parqueadero.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Genera y valida los tokens JWT. Un JWT es solo texto firmado: contiene el
 * usuario y una fecha de expiracion, y el servidor confirma que no fue
 * alterado verificando la firma con la misma clave secreta.
 */
@Service
public class JwtService {

    private final SecretKey llaveSecreta;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secreto,
                       @Value("${jwt.expiration-ms}") long expiracionMs) {
        this.llaveSecreta = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(String username) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(llaveSecreta)
                .compact();
    }

    public String extraerUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(llaveSecreta)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
}
