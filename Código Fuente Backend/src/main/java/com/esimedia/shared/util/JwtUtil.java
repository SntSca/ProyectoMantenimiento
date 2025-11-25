package com.esimedia.shared.util;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    public static final String USERNAME_CLAIM = "username";
    public static final String CREDENTIALS_VERSION_CLAIM = "credentialsVersion";
    public static final String ROL_CLAIM = "rol";
    public static final String TIPO_CONTENIDO_CLAIM = "tipoContenido";


    private final Key key;

    public JwtUtil(@Value("${token.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Overload: generar token usando el id de usuario como String (por ejemplo idUsuario de Mongo)
    public String generateToken(String userId, String username, long credentialsVersion, String rol, String tipoContenido) {
        long expirationMs = 24L * 60 * 60 * 1000;
        return Jwts.builder()
                .setSubject(userId)
                .claim(USERNAME_CLAIM, username)
                .claim(CREDENTIALS_VERSION_CLAIM, credentialsVersion)
                .claim(ROL_CLAIM, rol)
                .claim(TIPO_CONTENIDO_CLAIM, tipoContenido)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String extractTokenFromHeaderInstance(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Header de autorización inválido");
        }
        return authHeader.substring(7);
    }

    public String generateToken(String userId, String username, long credentialsVersion, Rol role) {
        return generateToken(userId, username, credentialsVersion, role.getValor(), null);
    }

    //Overload
    public String generateToken(String userId, String username, long credentialsVersion, Rol role, TipoContenido tipoContenido) {
        return generateToken(userId, username, credentialsVersion, role.getValor(), tipoContenido.getValor());
    }

    public Claims validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserIdFromToken(String token) {
        String extractedToken = extractTokenFromHeaderInstance(token);
        Claims claims = validateToken(extractedToken);
        return claims.getSubject();
    }

    public String getUsernameFromToken(String token) {
        String extractedToken = extractTokenFromHeaderInstance(token);
        Claims claims = validateToken(extractedToken);
        return claims.get(USERNAME_CLAIM, String.class);
    }

    public long getCredentialsVersionFromToken(String token) {
        String extractedToken = extractTokenFromHeaderInstance(token);
        Claims claims = validateToken(extractedToken);
        return claims.get(CREDENTIALS_VERSION_CLAIM, Long.class);
    }

    public String getRolFromToken(String token) {
        String extractedToken = extractTokenFromHeaderInstance(token);
        Claims claims = validateToken(extractedToken);
        return claims.get(ROL_CLAIM, String.class);
    }

    public String getTipoContenidoFromToken(String token) {
        String extractedToken = extractTokenFromHeaderInstance(token);
        Claims claims = validateToken(extractedToken);
        return claims.get(TIPO_CONTENIDO_CLAIM, String.class);
    }
}
