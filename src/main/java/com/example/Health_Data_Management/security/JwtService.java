package com.example.Health_Data_Management.security;



import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey secretKey;

    // Token validity: 24 hours
    private final long expirationTime = 24 * 60 * 60 * 1000;

    public JwtService(
            @Value("${jwt.secret}") String secret) {

        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                    "jwt.secret must contain at least 32 characters"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // ---------------------------------------------------------
    // GENERATE TOKEN
    // ---------------------------------------------------------

    public String generateToken(String username) {

        Date currentDate = new Date();

        Date expirationDate =
                new Date(currentDate.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .signWith(
                        secretKey,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    // ---------------------------------------------------------
    // EXTRACT USERNAME
    // ---------------------------------------------------------

    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    // ---------------------------------------------------------
    // EXTRACT EXPIRATION
    // ---------------------------------------------------------

    public Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    // ---------------------------------------------------------
    // EXTRACT CLAIM
    // ---------------------------------------------------------

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    // ---------------------------------------------------------
    // EXTRACT ALL CLAIMS
    // ---------------------------------------------------------

    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ---------------------------------------------------------
    // CHECK TOKEN EXPIRATION
    // ---------------------------------------------------------

    public boolean isTokenExpired(String token) {

        try {

            Date expiration =
                    extractExpiration(token);

            return expiration.before(new Date());

        } catch (Exception e) {

            return true;
        }
    }

    // ---------------------------------------------------------
    // VALIDATE TOKEN
    // ---------------------------------------------------------

    public boolean isTokenValid(
            String token,
            String username) {

        try {

            String tokenUsername =
                    extractUsername(token);

            return tokenUsername.equals(username)
                    && !isTokenExpired(token);

        } catch (Exception e) {

            return false;
        }
    }
}
