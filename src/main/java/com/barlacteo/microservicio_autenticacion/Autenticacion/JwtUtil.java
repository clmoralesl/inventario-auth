package com.barlacteo.microservicio_autenticacion.Autenticacion;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

@Component
public class JwtUtil {

    // Usa una clave secreta fija y segura (mínimo 512 bits para HS512)
    private static final String SECRET = "b8e2c2e5f7a1d4c9e3b6a7f8c1d2e3f4b5c6a7d8e9f0b1c2d3e4f5a6b7c8d9e0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5";
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 8;

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY) // Solo pasa la clave, no el algoritmo
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody().getExpiration().before(new Date());
    }
}