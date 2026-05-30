package com.techforum.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Minimum secret length required for HS256 (256 bits = 32 bytes)
    private static final int MIN_SECRET_LENGTH = 32;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    // FIX: Validate secret length at startup so misconfigured deployments
    //      fail fast with a clear error instead of signing weak tokens silently.
    //      Previously the secret "techforum_super_secret_key_2024" (38 chars)
    //      was borderline — using an env var now means production should supply
    //      a proper 32+ char random secret (see application.properties).
    @PostConstruct
    public void validateSecret() {
        if (jwtSecret == null || jwtSecret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT secret must be at least " + MIN_SECRET_LENGTH
                    + " characters long. Current length: "
                    + (jwtSecret == null ? 0 : jwtSecret.length())
                    + ". Set the JWT_SECRET environment variable.");
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Generate JWT token from authentication object
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return buildToken(userPrincipal.getUsername());
    }

    // Generate JWT token directly from username
    public String generateTokenFromUsername(String username) {
        return buildToken(username);
    }

    private String buildToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // Extract username from JWT token
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validate JWT token — returns false for any invalid/expired token
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
