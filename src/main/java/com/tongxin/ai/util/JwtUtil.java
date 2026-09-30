package com.tongxin.ai.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 工具
 * 秘钥与过期时间从 application.yaml 读取，避免硬编码
 *
 * @author wyq
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    /**
     * 生成 token，将 userId 与 username 写入 claims
     */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, "user");
    }

    /**
     * 生成 token（带角色），role 为 "user" 或 "admin"
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationTime);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 token 解析 userId
     */
    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }

    /**
     * 从 token 解析 username
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从 token 解析角色：user / admin
     */
    public String extractRole(String token) {
        Object role = parseClaims(token).get("role");
        return role == null ? "user" : role.toString();
    }

    /**
     * 校验 token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
