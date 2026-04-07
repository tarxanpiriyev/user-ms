package az.user_ms.service;

import az.user_ms.config.JwtProperties;
import az.user_ms.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enhanced JWT service with roles claim for deal-ms compatibility.
 */
@Service
@RequiredArgsConstructor
public class JWTService {

    private final JwtProperties jwtProperties;

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

    /**
     * Generate JWT with roles claim for deal-ms compatibility.
     * Token includes:
     * - sub: userId (UUID)
     * - roles: array of role names
     * - iss: issuer URI
     * - aud: audience (deal-ms)
     */
    public String generateToken(User user, boolean isRefreshToken) {
        Map<String, Object> claims = new HashMap<>();

        // Add userId as subject (UUID string)
        claims.put("sub", user.getId().toString());

        // Add roles array (compatible with deal-ms SecurityConfig)
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());
        claims.put("roles", roles);

        return createToken(claims, user.getEmail(), isRefreshToken);
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

    private String createToken(Map<String, Object> claims, String subject, boolean isRefreshToken) {
        int expiryMinutes = isRefreshToken ? jwtProperties.getRefreshTokenExpiryMinutes()
                : jwtProperties.getAccessTokenExpiryMinutes();

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * expiryMinutes))
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .signWith(getSignKey())
                .compact();
    }

    @SneakyThrows
    private SecretKey getSignKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hashedKey = sha256.digest(keyBytes);
        return Keys.hmacShaKeyFor(hashedKey);
    }
}
