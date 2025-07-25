package com.siddharthgawas.apigateway.service.impl;

import com.siddharthgawas.apigateway.configuration.ApplicationProperties;
import com.siddharthgawas.apigateway.service.JWTService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Service
@Log4j2
public class JWTServiceImpl implements JWTService {

    private final ApplicationProperties applicationProperties;

    private final SecretKey secretKey;

    public JWTServiceImpl(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.secretKey = Jwts.SIG.HS512.key()
                .random(new SecureRandom(applicationProperties.getSecret().getBytes(StandardCharsets.UTF_8)))
                .build();
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        final var currentDate = new Date();
        final var expirationDate = new Date(currentDate.getTime() + applicationProperties.getJwtAccessTokenExpirationMs());
        return Jwts.builder().subject(userDetails.getUsername())
                .issuedAt(currentDate)
                .id(UUID.randomUUID().toString())
                .issuer(applicationProperties.getJwtIssuer())
                .expiration(expirationDate)
                .signWith(this.secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        final var currentDate = new Date();
        final var expirationDate = new Date(currentDate.getTime() + applicationProperties.getJwtRefreshTokenExpirationMs());
        return Jwts.builder().subject(userDetails.getUsername())
                .issuedAt(currentDate)
                .id(UUID.randomUUID().toString())
                .issuer(applicationProperties.getJwtIssuer())
                .expiration(expirationDate)
                .signWith(this.secretKey)
                .compact();
    }


    @Override
    public Boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build()
                    .parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            log.error("JWT token is invalid: {}", exception.getMessage());
            return false;
        }
    }

    @Override
    public User getUserFromToken(String token) {
        try {
            var claims = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token).getPayload();
            String username = claims.getSubject();
            return new User(username, "", Collections.emptyList());
        } catch (JwtException | IllegalArgumentException exception) {
            log.error("JWT token is invalid: {}", exception.getMessage());
            return null;
        }
    }
}
