package com.lingxi.service;

import com.lingxi.util.JiutianTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 九天平台Token管理服务
 * 负责Token的生成、缓存、自动刷新
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JiutianTokenService {

    private final JiutianTokenUtil tokenUtil;
    
    // Token缓存：apiKey -> TokenCache
    private final ConcurrentHashMap<String, TokenCache> tokenCache = new ConcurrentHashMap<>();
    
    // 锁映射：apiKey -> Lock
    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    /**
     * 获取有效的Token
     * 如果Token不存在或即将过期，会自动生成新Token
     * 
     * @param apiKey API密钥
     * @return 有效的JWT Token
     */
    public String getValidToken(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key不能为空");
        }

        // 获取或创建锁
        ReentrantLock lock = lockMap.computeIfAbsent(apiKey, k -> new ReentrantLock());
        
        lock.lock();
        try {
            TokenCache cache = tokenCache.get(apiKey);
            
            // 检查缓存的Token是否有效
            if (cache != null && cache.isValid()) {
                String[] parts = apiKey.split("\\.", 2);
                String secret = parts.length > 1 ? parts[1] : "";
                if (!tokenUtil.shouldRefreshToken(cache.getToken(), secret)) {
                    log.debug("Using cached token for API key: {}", getMaskedApiKey(apiKey));
                    return cache.getToken();
                }
            }
            
            // 生成新Token
            log.info("Generating new token for API key: {}", getMaskedApiKey(apiKey));
            String[] parts = apiKey.split("\\.", 2);
            String secret = parts.length > 1 ? parts[1] : "";
            String newToken = tokenUtil.generateToken(apiKey, secret);
            
            // 缓存新Token
            long expirationTime = Instant.now().getEpochSecond() + 3600; // 1小时后过期
            tokenCache.put(apiKey, new TokenCache(newToken, expirationTime));
            
            return newToken;
            
        } catch (Exception e) {
            log.error("Failed to get valid token for API key: {}", getMaskedApiKey(apiKey), e);
            throw new RuntimeException("获取Token失败: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 强制刷新Token
     * 
     * @param apiKey API密钥
     * @return 新的JWT Token
     */
    public String refreshToken(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key不能为空");
        }

        ReentrantLock lock = lockMap.computeIfAbsent(apiKey, k -> new ReentrantLock());
        
        lock.lock();
        try {
            log.info("Force refreshing token for API key: {}", getMaskedApiKey(apiKey));
            
            // 移除旧缓存
            tokenCache.remove(apiKey);
            
            // 生成新Token
            String[] parts = apiKey.split("\\.", 2);
            String secret = parts.length > 1 ? parts[1] : "";
            String newToken = tokenUtil.generateToken(apiKey, secret);
            
            // 缓存新Token
            long expirationTime = Instant.now().getEpochSecond() + 3600;
            tokenCache.put(apiKey, new TokenCache(newToken, expirationTime));
            
            return newToken;
            
        } catch (Exception e) {
            log.error("Failed to refresh token for API key: {}", getMaskedApiKey(apiKey), e);
            throw new RuntimeException("刷新Token失败: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检查Token是否需要刷新
     * 
     * @param apiKey API密钥
     * @return 是否需要刷新
     */
    public boolean shouldRefreshToken(String apiKey) {
        TokenCache cache = tokenCache.get(apiKey);
        if (cache == null) {
            return true;
        }
        return cache.shouldRefresh();
    }

    /**
     * 获取Token剩余时间
     * 
     * @param apiKey API密钥
     * @return 剩余秒数
     */
    public long getTokenRemainingTime(String apiKey) {
        TokenCache cache = tokenCache.get(apiKey);
        if (cache == null) {
            return 0;
        }
        return cache.getRemainingTime();
    }

    /**
     * 清除指定API Key的Token缓存
     * 
     * @param apiKey API密钥
     */
    public void clearTokenCache(String apiKey) {
        tokenCache.remove(apiKey);
        log.info("Cleared token cache for API key: {}", getMaskedApiKey(apiKey));
    }

    /**
     * 清除所有Token缓存
     */
    public void clearAllTokenCache() {
        int size = tokenCache.size();
        tokenCache.clear();
        log.info("Cleared all token cache, {} entries removed", size);
    }

    /**
     * 定时清理过期Token缓存
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void cleanupExpiredTokens() {
        long currentTime = Instant.now().getEpochSecond();
        int removedCount = 0;
        
        for (String apiKey : tokenCache.keySet()) {
            TokenCache cache = tokenCache.get(apiKey);
            if (cache != null && cache.getExpirationTime() <= currentTime) {
                tokenCache.remove(apiKey);
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            log.info("Cleaned up {} expired tokens", removedCount);
        }
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 统计信息
     */
    public TokenCacheStats getCacheStats() {
        int totalTokens = tokenCache.size();
        int validTokens = 0;
        int expiringSoon = 0;
        
        long currentTime = Instant.now().getEpochSecond();
        
        for (TokenCache cache : tokenCache.values()) {
            if (cache.isValid()) {
                validTokens++;
                if (cache.shouldRefresh()) {
                    expiringSoon++;
                }
            }
        }
        
        return new TokenCacheStats(totalTokens, validTokens, expiringSoon);
    }

    /**
     * 掩码API Key用于日志输出
     */
    private String getMaskedApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
    }

    /**
     * Token缓存类
     */
    private static class TokenCache {
        private final String token;
        private final long expirationTime;
        private final long createdTime;

        public TokenCache(String token, long expirationTime) {
            this.token = token;
            this.expirationTime = expirationTime;
            this.createdTime = Instant.now().getEpochSecond();
        }

        public String getToken() {
            return token;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public boolean isValid() {
            return Instant.now().getEpochSecond() < expirationTime;
        }

        public boolean shouldRefresh() {
            long currentTime = Instant.now().getEpochSecond();
            return (expirationTime - currentTime) < 300; // 5分钟内过期需要刷新
        }

        public long getRemainingTime() {
            return Math.max(0, expirationTime - Instant.now().getEpochSecond());
        }
    }

    /**
     * Token缓存统计信息
     */
    public static class TokenCacheStats {
        private final int totalTokens;
        private final int validTokens;
        private final int expiringSoon;

        public TokenCacheStats(int totalTokens, int validTokens, int expiringSoon) {
            this.totalTokens = totalTokens;
            this.validTokens = validTokens;
            this.expiringSoon = expiringSoon;
        }

        public int getTotalTokens() {
            return totalTokens;
        }

        public int getValidTokens() {
            return validTokens;
        }

        public int getExpiringSoon() {
            return expiringSoon;
        }

        @Override
        public String toString() {
            return String.format("TokenCacheStats{total=%d, valid=%d, expiringSoon=%d}", 
                    totalTokens, validTokens, expiringSoon);
        }
    }
}