
package com.vivekanand.manager.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
  private final Key key; private final String issuer; private final int expirationMinutes;
  public JwtUtil(@Value("${jwt.secret}") String secret,
                 @Value("${jwt.issuer}") String issuer,
                 @Value("${jwt.expirationMinutes}") int expirationMinutes) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes()); this.issuer = issuer; this.expirationMinutes = expirationMinutes;
  }
  public String generateToken(String username, String role) {
    Instant now = Instant.now();
    return Jwts.builder().setSubject(username).setIssuer(issuer)
      .setIssuedAt(Date.from(now))
      .setExpiration(Date.from(now.plusSeconds(expirationMinutes * 60L)))
      .addClaims(Map.of("role", role)).signWith(key, SignatureAlgorithm.HS256).compact();
  }
  public Jws<Claims> parse(String jwt) { return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt); }
}
