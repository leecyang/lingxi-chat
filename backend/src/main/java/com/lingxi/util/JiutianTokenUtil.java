package com.lingxi.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 九天平台JWT Token生成工具类
 * 根据九天大模型应用平台用户使用手册实现
 */
@Slf4j
@Component
public class JiutianTokenUtil {

    /**
     * Token有效期（1小时）
     */
    private static final long TOKEN_VALIDITY = 3600; // 1小时

    /**
     * 生成九天平台JWT Token
     * 
     * @param apiKey API密钥，格式为 "kid.secret"
     * @return JWT Token
     */
    public String generateToken(String apiKey) {
        return generateToken(apiKey, null);
    }
    
    /**
     * 生成九天平台JWT Token
     * 根据九天大模型应用平台用户使用手册的要求生成Token
     * 
     * @param apiKey API密钥，格式为 "kid.secret"
     * @param secretOverride 可选的密钥覆盖
     * @return JWT Token
     */
    public String generateToken(String apiKey, String secretOverride) {
        if (apiKey == null || !apiKey.contains(".")) {
            throw new IllegalArgumentException("API Key格式错误，应为 kid.secret 格式");
        }

        try {
            String[] parts = apiKey.split("\\.", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("API Key格式错误，应为 kid.secret 格式");
            }

            String kid = parts[0];  // API Key ID
            String secret = secretOverride != null ? secretOverride : parts[1];  // Secret

            // 当前时间戳（秒）
            long currentTime = Instant.now().getEpochSecond();
            long expirationTime = currentTime + TOKEN_VALIDITY;

            // 创建密钥
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            // 创建自定义Header，符合九天平台要求
            Map<String, Object> headerMap = new HashMap<>();
            headerMap.put("alg", "HS256");
            headerMap.put("typ", "JWT");
            headerMap.put("sign_type", "SIGN");

            // 生成JWT Token，按照九天平台文档要求
            String token = Jwts.builder()
                    .header().add(headerMap).and()
                    .claim("api_key", kid)
                    .claim("exp", expirationTime)
                    .claim("timestamp", currentTime)
                    .signWith(key)
                    .compact();

            log.debug("Generated JWT token for API key: {}, expires at: {}", kid, new Date(expirationTime * 1000));
            return token;

        } catch (Exception e) {
            log.error("Failed to generate JWT token for API key: {}", apiKey, e);
            throw new RuntimeException("生成JWT Token失败: " + e.getMessage());
        }
    }

    /**
     * 检查Token是否即将过期（剩余时间少于5分钟）
     * 
     * @param token JWT Token
     * @param secret 用于验证的密钥
     * @return 是否需要刷新
     */
    public boolean shouldRefreshToken(String token, String secret) {
        if (token == null || token.trim().isEmpty()) {
            return true;
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // 从自定义的exp字段获取过期时间（秒级时间戳）
            Object expClaim = claims.get("exp");
            if (expClaim == null) {
                log.warn("Token中缺少exp字段");
                return true;
            }
            
            long expirationTime;
            if (expClaim instanceof Number) {
                expirationTime = ((Number) expClaim).longValue() * 1000; // 转换为毫秒
            } else {
                log.warn("Token中exp字段格式错误: {}", expClaim);
                return true;
            }
            
            long remainingTime = expirationTime - System.currentTimeMillis();
            
            // 如果剩余时间少于5分钟（300秒），则需要刷新
            return remainingTime < 300000;
            
        } catch (Exception e) {
            log.warn("Failed to check token expiration: {}", e.getMessage());
            return true; // 解析失败时默认需要刷新
        }
    }

    /**
     * 获取Token剩余有效时间（秒）
     * 
     * @param token JWT Token
     * @param secret 用于验证的密钥
     * @return 剩余秒数，-1表示解析失败
     */
    public long getTokenRemainingTime(String token, String secret) {
        if (token == null || token.trim().isEmpty()) {
            return -1;
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // 从自定义的exp字段获取过期时间（秒级时间戳）
            Object expClaim = claims.get("exp");
            if (expClaim == null) {
                log.warn("Token中缺少exp字段");
                return -1;
            }
            
            long expirationTime;
            if (expClaim instanceof Number) {
                expirationTime = ((Number) expClaim).longValue() * 1000; // 转换为毫秒
            } else {
                log.warn("Token中exp字段格式错误: {}", expClaim);
                return -1;
            }
            
            long remainingTime = expirationTime - System.currentTimeMillis();
            
            return Math.max(0, remainingTime / 1000);
            
        } catch (Exception e) {
            log.warn("Failed to get token remaining time: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Token信息类
     */
    public static class TokenInfo {
        private final String token;
        private final long expirationTime;
        private final boolean isValid;

        public TokenInfo(String token, long expirationTime, boolean isValid) {
            this.token = token;
            this.expirationTime = expirationTime;
            this.isValid = isValid;
        }

        public String getToken() {
            return token;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public boolean isValid() {
            return isValid;
        }

        public boolean shouldRefresh() {
            long currentTime = Instant.now().getEpochSecond();
            return (expirationTime - currentTime) < 300; // 5分钟内过期
        }
    }
}