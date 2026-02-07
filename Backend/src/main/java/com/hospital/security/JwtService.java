package com.hospital.security;

import com.hospital.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${app.jwt.secret}")
  private String secret;

  @Value("${app.jwt.expiration-ms:86400000}")
  private long expirationMs;

  public String generateToken(String email, Role role) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(email)
        .claim("role", role.name())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusMillis(expirationMs)))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean isTokenValid(String token) {
    try {
      parseAllClaims(token);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public String extractEmail(String token) {
    return parseAllClaims(token).getSubject();
  }

  private Claims parseAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSigningKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
