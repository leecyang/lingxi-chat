package com.lingxi.service;

import com.lingxi.util.JiutianTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 九天平台Token验证服务
 * 用于验证生成的Token是否符合九天平台要求
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JiutianTokenValidationService {

    private final JiutianTokenUtil tokenUtil;
    private final ObjectMapper objectMapper;

    /**
     * 验证Token格式是否符合九天平台要求
     * 
     * @param apiKey API密钥
     * @return 验证结果
     */
    public TokenValidationResult validateToken(String apiKey) {
        try {
            // 生成Token
            String token = tokenUtil.generateToken(apiKey);
            log.info("Generated token: {}", token);
            
            // 解析Token结构
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                return new TokenValidationResult(false, "Token格式错误：应包含3个部分", null);
            }
            
            // 解析Header
            String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
            JsonNode headerNode = objectMapper.readTree(headerJson);
            log.info("Token Header: {}", headerJson);
            
            // 验证Header字段
            if (!"HS256".equals(headerNode.get("alg").asText())) {
                return new TokenValidationResult(false, "Header中alg字段应为HS256", token);
            }
            if (!"JWT".equals(headerNode.get("typ").asText())) {
                return new TokenValidationResult(false, "Header中typ字段应为JWT", token);
            }
            if (!"SIGN".equals(headerNode.get("sign_type").asText())) {
                return new TokenValidationResult(false, "Header中sign_type字段应为SIGN", token);
            }
            
            // 解析Payload
            String payloadJson = new String(Base64.getUrlDecoder().decode(tokenParts[1]));
            JsonNode payloadNode = objectMapper.readTree(payloadJson);
            log.info("Token Payload: {}", payloadJson);
            
            // 验证Payload字段
            if (!payloadNode.has("api_key")) {
                return new TokenValidationResult(false, "Payload中缺少api_key字段", token);
            }
            if (!payloadNode.has("exp")) {
                return new TokenValidationResult(false, "Payload中缺少exp字段", token);
            }
            if (!payloadNode.has("timestamp")) {
                return new TokenValidationResult(false, "Payload中缺少timestamp字段", token);
            }
            
            // 验证api_key值
            String[] apiKeyParts = apiKey.split("\\.", 2);
            String expectedKid = apiKeyParts[0];
            if (!expectedKid.equals(payloadNode.get("api_key").asText())) {
                return new TokenValidationResult(false, "Payload中api_key值不正确", token);
            }
            
            // 验证时间字段
            long exp = payloadNode.get("exp").asLong();
            long timestamp = payloadNode.get("timestamp").asLong();
            long currentTime = System.currentTimeMillis() / 1000;
            
            if (timestamp > currentTime + 60) { // 允许1分钟的时间偏差
                return new TokenValidationResult(false, "timestamp时间不正确", token);
            }
            
            if (exp <= currentTime) {
                return new TokenValidationResult(false, "Token已过期", token);
            }
            
            if (exp - timestamp != 3600) {
                return new TokenValidationResult(false, "Token有效期应为1小时", token);
            }
            
            return new TokenValidationResult(true, "Token格式正确，符合九天平台要求", token);
            
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return new TokenValidationResult(false, "Token验证异常: " + e.getMessage(), null);
        }
    }
    
    /**
     * 测试Token刷新机制
     */
    public void testTokenRefresh(String apiKey) {
        try {
            log.info("=== 开始测试Token刷新机制 ===");
            
            // 生成初始Token
            String token1 = tokenUtil.generateToken(apiKey);
            log.info("初始Token: {}", token1);
            
            // 等待1秒
            Thread.sleep(1000);
            
            // 生成新Token
            String token2 = tokenUtil.generateToken(apiKey);
            log.info("新Token: {}", token2);
            
            // 验证Token是否不同
            if (token1.equals(token2)) {
                log.warn("警告：两次生成的Token相同，可能存在问题");
            } else {
                log.info("Token刷新正常：两次生成的Token不同");
            }
            
            // 测试Token过期检查
            String[] parts = apiKey.split("\\.", 2);
            String secret = parts.length > 1 ? parts[1] : "";
            
            boolean shouldRefresh = tokenUtil.shouldRefreshToken(token2, secret);
            log.info("Token是否需要刷新: {}", shouldRefresh);
            
            long remainingTime = tokenUtil.getTokenRemainingTime(token2, secret);
            log.info("Token剩余时间: {} 秒", remainingTime);
            
            log.info("=== Token刷新机制测试完成 ===");
            
        } catch (Exception e) {
            log.error("Token刷新测试失败", e);
        }
    }
    
    /**
     * Token验证结果
     */
    public static class TokenValidationResult {
        private final boolean valid;
        private final String message;
        private final String token;
        
        public TokenValidationResult(boolean valid, String message, String token) {
            this.valid = valid;
            this.message = message;
            this.token = token;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getToken() {
            return token;
        }
        
        @Override
        public String toString() {
            return String.format("TokenValidationResult{valid=%s, message='%s', token='%s'}", 
                    valid, message, token != null ? token.substring(0, Math.min(50, token.length())) + "..." : "null");
        }
    }
}