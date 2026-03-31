package com.project.lms.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(UserDetails userDetails){

        log.info("Generating JWT token for user {}", userDetails.getUsername());

        String token = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(
                        Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)),
                        Jwts.SIG.HS256
                )
                .compact();

        log.debug("JWT token generated successfully for user {}", userDetails.getUsername());

        return token;
    }

    public boolean validateToken(String token, UserDetails userDetails){

        log.debug("Validating JWT token for user {}", userDetails.getUsername());

        boolean isValid = extractUserName(token).equals(userDetails.getUsername());

        if (isValid) {
            log.info("JWT token validated successfully for user {}", userDetails.getUsername());
        } else {
            log.warn("JWT token validation failed for user {}", userDetails.getUsername());
        }

        return isValid;
    }

    public String extractUserName(String token){

        log.debug("Extracting username from JWT token");

        try {

            String username = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            log.debug("Username extracted from token: {}", username);

            return username;

        } catch (JwtException e) {

            log.error("Error extracting username from token: {}", e.getMessage());
            throw e;
        }
    }
}