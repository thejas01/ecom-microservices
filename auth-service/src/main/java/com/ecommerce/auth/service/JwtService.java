package com.ecommerce.auth.service;

import com.ecommerce.auth.entity.UserCredential;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private static final Long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(UserCredential userCredential) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userCredential.getId());
        claims.put("role", userCredential.getRole().name());
        claims.put("email", userCredential.getEmail());
        
        return createToken(claims, userCredential.getUsername(), jwtExpiration);
    }

    public String generateRefreshToken(UserCredential userCredential) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userCredential.getId());
        claims.put("tokenType", "REFRESH");
        
        return createToken(claims, userCredential.getUsername(), REFRESH_TOKEN_EXPIRATION);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public String getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get("userId", String.class);
    }

    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email", String.class);
    }

    public Date getExpirationFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return getExpirationFromToken(token).before(new Date());
    }

    public boolean isRefreshToken(String token) {
        String tokenType = getClaimsFromToken(token).get("tokenType", String.class);
        return "REFRESH".equals(tokenType);
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getAccessTokenExpiration() {
        return jwtExpiration / 1000; // Convert to seconds
    }

    public Long getRefreshTokenExpiration() {
        return REFRESH_TOKEN_EXPIRATION / 1000; // Convert to seconds
    }
}