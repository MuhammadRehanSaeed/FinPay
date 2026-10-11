package com.rehancode.finpay.JWT;


import com.rehancode.finpay.Model.UsersModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String securityKey;

    public SecretKey getKey() {
        return Keys.hmacShaKeyFor(securityKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UsersModel usersModel) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", usersModel.getId());
        Date now = new Date(System.currentTimeMillis());
        long expirationTime = 30 * 60 * 1000; // 30 minutes
        Date exp = new Date(System.currentTimeMillis() + expirationTime);
        return Jwts.builder()
                .claims(claims)
                .subject(usersModel.getUsername())
                .issuedAt(now)
                .id(UUID.randomUUID().toString())
                .expiration(exp)
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    public boolean isExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isExpired(token));
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
