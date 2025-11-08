package com.order.util;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class OrderUtil {

	@Value("${jwt.secret-key}")
	public String SECRET; 

	public String extractUsernameFromBearerToken(HttpServletRequest request) {
        String token = getTokenFromHeader(request);
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token not found");
        }
        Claims claims = extractClaims(token);
        return claims.getSubject();
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET) 
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Invalid token signature");
        }
    }
}
