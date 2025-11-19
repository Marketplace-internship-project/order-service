package io.hohichh.marketplace.order.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;


@Component
@Slf4j
public class JwtValidator {
    private final SecretKey accessSecret;

    public JwtValidator(@Value("${jwt.access.secret}") String accessSecretStr) {
        accessSecret = Keys.hmacShaKeyFor(accessSecretStr.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validate(String token) {
        try{
            Jwts.parser()
                    .verifyWith(accessSecret)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("\"Invalid JWT token: {}\"", e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(accessSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
