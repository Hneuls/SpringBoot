package com.web.board.config;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

@Component
public class JwtTokenProvider {    

    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createToken(String username, String nickname) {
        byte[] keyBytes = jwtProperties.getSecret().getBytes();
        Key signingKey = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("nickname", nickname);
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtProperties.getSecret().getBytes())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();            
        return claims.getSubject();
    }

    public String getNickname(String token) {
        Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtProperties.getSecret().getBytes())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();            
        return claims.get("nickname", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(jwtProperties.getSecret().getBytes())
                .build()
                .parseClaimsJws(token);
                return true;
        } catch (Exception e) {
            return false;
        }
    }
}
