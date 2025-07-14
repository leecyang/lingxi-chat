package com.lingxi.controller;

import com.lingxi.service.JiutianTokenService;
import com.lingxi.service.JiutianTokenValidationService;
import com.lingxi.util.JiutianTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Token测试控制器
 * 用于测试九天平台Token的生成和管理功能
 */
@Slf4j
@RestController
@RequestMapping("/api/test/token")
@RequiredArgsConstructor
public class TokenTestController {

    private final JiutianTokenService tokenService;
    private final JiutianTokenUtil tokenUtil;
    private final JiutianTokenValidationService validationService;

    /**
     * 测试Token生成
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateToken(@RequestParam String apiKey) {
        try {
            String token = tokenService.getValidToken(apiKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("message", "Token生成成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Token生成失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Token生成失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 测试Token刷新
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestParam String apiKey) {
        try {
            String newToken = tokenService.refreshToken(apiKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", newToken);
            response.put("message", "Token刷新成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Token刷新失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Token刷新失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查Token状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkTokenStatus(@RequestParam String apiKey) {
        try {
            boolean shouldRefresh = tokenService.shouldRefreshToken(apiKey);
            long remainingTime = tokenService.getTokenRemainingTime(apiKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("shouldRefresh", shouldRefresh);
            response.put("remainingTime", remainingTime);
            response.put("message", "Token状态检查成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Token状态检查失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Token状态检查失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache-stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        try {
            JiutianTokenService.TokenCacheStats stats = tokenService.getCacheStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            response.put("message", "缓存统计获取成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("缓存统计获取失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "缓存统计获取失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 清除Token缓存
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, Object>> clearTokenCache(@RequestParam(required = false) String apiKey) {
        try {
            if (apiKey != null) {
                tokenService.clearTokenCache(apiKey);
            } else {
                tokenService.clearAllTokenCache();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "缓存清除成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("缓存清除失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "缓存清除失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 验证Token格式是否符合九天平台要求
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String apiKey) {
        try {
            JiutianTokenValidationService.TokenValidationResult result = validationService.validateToken(apiKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isValid());
            response.put("valid", result.isValid());
            response.put("message", result.getMessage());
            if (result.getToken() != null) {
                response.put("token", result.getToken());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Token验证失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("valid", false);
            response.put("message", "Token验证失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 测试Token刷新机制
     */
    @PostMapping("/test-refresh")
    public ResponseEntity<Map<String, Object>> testTokenRefresh(@RequestParam String apiKey) {
        try {
            validationService.testTokenRefresh(apiKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Token刷新机制测试完成，请查看日志获取详细信息");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Token刷新机制测试失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Token刷新机制测试失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 测试九天API调用（使用示例API Key）
     */
    @PostMapping("/test-api-call")
    public ResponseEntity<Map<String, Object>> testApiCall() {
        try {
            // 使用文档中的示例API Key
            String exampleApiKey = "646ae749bcf5bc1a1498aeaf.lbIpYGaWQ8VwQ2HYTOkhDCKJP/aGgGAc";
            
            // 验证Token格式
            JiutianTokenValidationService.TokenValidationResult validationResult = validationService.validateToken(exampleApiKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tokenValidation", validationResult);
            response.put("message", "API调用测试完成");
            
            if (validationResult.isValid()) {
                response.put("recommendation", "Token格式正确，可以尝试调用九天API");
            } else {
                response.put("recommendation", "Token格式有问题，需要修复: " + validationResult.getMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("API调用测试失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "API调用测试失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}