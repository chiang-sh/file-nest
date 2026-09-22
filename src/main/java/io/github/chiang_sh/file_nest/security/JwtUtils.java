package io.github.chiang_sh.file_nest.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

@Component
public class JwtUtils {
    static final String JWT_HEADER = "Authorization";
    static final String TOKEN_PREFIX = "Bearer ";
    static final long EXPIRATION_TIME = 60 * 60 * 1000;
    final SecretKey key;

    public JwtUtils(@Value("${security.jwt.secret-base64}") String secretBase64) {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Invalid JWT secret.");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + EXPIRATION_TIME);
        return Jwts.builder()
                .signWith(key)
                .subject(username)
                .issuedAt(now)
                .expiration(exp)
                .compact();
    }

    public Optional<String> getRequestToken(HttpServletRequest request) {
        String authHeader = request.getHeader(JWT_HEADER);
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
