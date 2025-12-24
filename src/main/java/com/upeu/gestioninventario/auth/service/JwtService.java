package com.upeu.gestioninventario.auth.service;

import com.upeu.gestioninventario.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private SecretKey signInKey;

    private static final long TOKEN_ACCESS_EXPIRATION = 1000 * 60 * 60 * 24;
    private final long TOKEN_REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 30;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secretKey());
        this.signInKey = Keys.hmacShaKeyFor(keyBytes);
        log.debug("Clave de firma JWT inicializada correctamente.");
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails.getUsername(), jwtProperties.expiration());
    }

    public String generateRefreshToken(UserDetails userDetails, boolean rememberMe) {
        long expirationTime = rememberMe ? TOKEN_REFRESH_EXPIRATION : TOKEN_ACCESS_EXPIRATION;
        return generateToken(new HashMap<>(), userDetails.getUsername(), expirationTime);
    }

    public String generateToken(Map<String, Object> extraClaims, String subject, long expirationInMillis) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationInMillis);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(signInKey)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Intento de validación de token fallido: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signInKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}