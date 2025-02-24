package com.freelance_platform.user_ms.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTService {
    private final String secretKey;

    private final int refreshToken;

    private final int accessToken;

    public JWTService(@Value("${spring.security.secret}") String secretKey
            , @Value("${spring.security.refreshToken-expires-in-minute}") int refreshToken
            , @Value("${spring.security.accessToken-expires-in-minute}") int accessToken) {
        this.secretKey = secretKey;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateToken(String username, boolean isRefreshToken) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, isRefreshToken);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    private String createToken(Map<String, Object> claims, String username, boolean isRefreshToken) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(
                        System.currentTimeMillis() + 1000L * 60 * (isRefreshToken ? refreshToken : accessToken)))
                .signWith(getSignKey()).compact();

    }

    @SneakyThrows
    private SecretKey getSignKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hashedKey = sha256.digest(keyBytes);
        return Keys.hmacShaKeyFor(hashedKey);

    }
}
