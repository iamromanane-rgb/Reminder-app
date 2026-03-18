package com.reminder.backend.auth;

import com.reminder.backend.models.User;
import com.reminder.backend.models.AccessLevel;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        AccessLevel accessLevel = user.getAccessLevel() == null ? AccessLevel.READ : user.getAccessLevel();

        return Jwts.builder() //builds the json object
                .subject(user.getEmail())
                .claim("userId", user.getId()) //pushes userid into the token
                .claim("username", user.getUsername())
                .claim("empId", user.getEmpId())
                .claim("accessLevel", accessLevel.getClaimValue())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)//final approval with hashed signkey
                .compact();//squashes everything together
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public AccessLevel getAccessLevel(String token) {
        Object value = parseClaims(token).get("accessLevel");
        return AccessLevel.fromInput(value == null ? null : value.toString());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();//if signature matches, it opens the tokens and reads the payload
    }
}
