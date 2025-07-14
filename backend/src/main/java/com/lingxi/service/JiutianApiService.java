package com.lingxi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingxi.entity.Agent;
import com.lingxi.entity.ChatHistory;
import com.lingxi.util.JiutianTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 九天API代理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JiutianApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final JiutianTokenService tokenService;
    private final JiutianTokenUtil tokenUtil;

    @Value("${app.jiutian.base-url}")
    private String jiutianBaseUrl;

    @Value("${app.jiutian.api-key}")
    private String apiKey;

    @Value("${app.jiutian.timeout:30000}")
    private int timeoutMs;

    @Value("${app.jiutian.max-retries:3}")
    private int maxRetries;

    @Value("${app.jiutian.retry-delay:1000}")
    private int retryDelayMs;

    /**
     * 发送消息到九天API
     */
    public CompletableFuture<String> sendMessage(Agent agent, String userMessage, List<ChatHistory> history) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendMessageSync(agent, userMessage, history);
            } catch (Exception e) {
                log.error("Error sending message to Jiutian API", e);
                throw new RuntimeException("调用九天API失败: " + e.getMessage());
            }
        });
    }

    /**
     * 同步发送消息
     */
    public String sendMessageSync(Agent agent, String userMessage, List<ChatHistory> history) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Sending message to Jiutian API, attempt: {}/{}", attempt, maxRetries);
                
                // 构建请求
                Map<String, Object> request = buildRequest(agent, userMessage, history);
                
                // 发送请求（使用智能体的API Key）
                String response = callJiutianApi(agent.getEndpoint(), request, agent.getApiKey());
                
                // 解析响应
                String content = parseResponse(response);
                
                log.info("Successfully received response from Jiutian API");
                return content;
                
            } catch (Exception e) {
                log.warn("Attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
                
                if (attempt == maxRetries) {
                    throw new RuntimeException("九天API调用失败，已重试" + maxRetries + "次: " + e.getMessage());
                }
                
                // 等待后重试
                try {
                    Thread.sleep(retryDelayMs * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断");
                }
            }
        }
        
        throw new RuntimeException("九天API调用失败");
    }

    /**
     * 流式发送消息
     */
    public CompletableFuture<Void> sendMessageStream(Agent agent, String userMessage, 
                                                   List<ChatHistory> history, 
                                                   StreamResponseHandler handler) {
        return CompletableFuture.runAsync(() -> {
            try {
                sendMessageStreamSync(agent, userMessage, history, handler);
            } catch (Exception e) {
                log.error("Error in stream message", e);
                handler.onError(e);
            }
        });
    }

    /**
     * 同步流式发送消息
     */
    public void sendMessageStreamSync(Agent agent, String userMessage, 
                                    List<ChatHistory> history, 
                                    StreamResponseHandler handler) {
        try {
            // 构建请求
            Map<String, Object> request = buildRequest(agent, userMessage, history);
            request.put("stream", true);
            
            // 模拟流式响应（实际应该使用SSE或WebSocket）
            String fullResponse = callJiutianApi(agent.getEndpoint(), request, agent.getApiKey());
            String content = parseResponse(fullResponse);
            
            // 分块发送
            String[] chunks = splitIntoChunks(content, 50);
            
            for (String chunk : chunks) {
                handler.onChunk(chunk);
                try {
                    Thread.sleep(100); // 模拟流式延迟
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            handler.onComplete();
            
        } catch (Exception e) {
            handler.onError(e);
        }
    }

    /**
     * 检查智能体健康状态
     */
    public boolean checkAgentHealth(Agent agent) {
        try {
            Map<String, Object> healthRequest = Map.of(
                    "prompt", "健康检查",
                    "appId", agent.getAppId(),
                    "history", List.of(),
                    "stream", false
            );
            
            String response = callJiutianApi(agent.getEndpoint(), healthRequest, agent.getApiKey());
            return response != null && !response.isEmpty();
            
        } catch (Exception e) {
            log.warn("Agent health check failed for {}: {}", agent.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * 获取模型信息
     */
    public Map<String, Object> getModelInfo(String modelId) {
        try {
            String url = jiutianBaseUrl + "/models/" + modelId;
            
            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return objectMapper.convertValue(jsonNode, Map.class);
            }
            
            return Map.of("error", "获取模型信息失败");
            
        } catch (Exception e) {
            log.error("Error getting model info for: {}", modelId, e);
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * 获取可用模型列表
     */
    public List<Map<String, Object>> getAvailableModels() {
        try {
            String url = jiutianBaseUrl + "/models";
            
            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                if (jsonNode.has("data") && jsonNode.get("data").isArray()) {
                    List<Map<String, Object>> models = new ArrayList<>();
                    for (JsonNode modelNode : jsonNode.get("data")) {
                        models.add(objectMapper.convertValue(modelNode, Map.class));
                    }
                    return models;
                }
            }
            
            return Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting available models", e);
            return Collections.emptyList();
        }
    }

    /**
     * 验证API密钥
     */
    public boolean validateApiKey(String testApiKey) {
        try {
            String url = jiutianBaseUrl + "/models";
            
            HttpHeaders headers = createHeaders(testApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            log.warn("API key validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取API使用统计
     */
    public Map<String, Object> getApiUsageStats() {
        try {
            String url = jiutianBaseUrl + "/usage";
            
            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return objectMapper.convertValue(jsonNode, Map.class);
            }
            
            return Map.of("error", "获取使用统计失败");
            
        } catch (Exception e) {
            log.error("Error getting API usage stats", e);
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * 构建九天平台请求体
     */
    private Map<String, Object> buildRequest(Agent agent, String userMessage, List<ChatHistory> history) {
        Map<String, Object> request = new HashMap<>();
        
        // 九天平台必需字段
        request.put("prompt", userMessage);
        request.put("appId", agent.getAppId());
        request.put("stream", true);
        
        // 构建历史对话记录
        List<Map<String, Object>> historyList = new ArrayList<>();
        if (history != null && !history.isEmpty()) {
            history.stream()
                    .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                    .limit(10) // 限制历史消息数量
                    .forEach(chat -> {
                        Map<String, Object> historyItem = new HashMap<>();
                        if (chat.getMessageType() == ChatHistory.MessageType.USER) {
                            historyItem.put("user", chat.getContent());
                        } else if (chat.getMessageType() == ChatHistory.MessageType.AGENT && chat.getContent() != null) {
                            historyItem.put("assistant", chat.getContent());
                        }
                        if (!historyItem.isEmpty()) {
                            historyList.add(historyItem);
                        }
                    });
        }
        request.put("history", historyList);
        
        return request;
    }

    /**
     * 调用九天API（带Token自动刷新）
     */
    private String callJiutianApi(String endpoint, Map<String, Object> request, String agentApiKey) {
        return callJiutianApiWithRetry(endpoint, request, agentApiKey, false);
    }
    
    /**
     * 调用九天API（支持重试和Token刷新）
     */
    private String callJiutianApiWithRetry(String endpoint, Map<String, Object> request, String agentApiKey, boolean isRetry) {
        try {
            // 使用正确的九天平台API端点
            String url = endpoint != null && !endpoint.isEmpty() ? endpoint : 
                    "https://jiutian.10086.cn/largemodel/api/v1/completions";
            
            // 获取有效的Token
            String effectiveApiKey = agentApiKey != null ? agentApiKey : apiKey;
            
            // 检查Token是否即将过期，如果是则先刷新
            if (effectiveApiKey != null && effectiveApiKey.contains(".")) {
                String[] parts = effectiveApiKey.split("\\.", 2);
                String secret = parts.length > 1 ? parts[1] : "";
                String currentToken = tokenService.getValidToken(effectiveApiKey);
                if (tokenUtil.shouldRefreshToken(currentToken, secret)) {
                    log.info("Token将要过期，预先刷新Token");
                    tokenService.refreshToken(effectiveApiKey);
                }
            }
            
            String validToken = tokenService.getValidToken(effectiveApiKey);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + validToken);
            
            String requestBody = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            log.debug("Calling Jiutian API: {} with request: {}", url, requestBody);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                String responseBody = response.getBody();
                log.warn("API调用返回非200状态码: {}, 响应: {}", response.getStatusCode(), responseBody);
                
                // 检查是否是认证错误
                if (isAuthenticationError(response.getStatusCode(), responseBody) && !isRetry) {
                    log.info("检测到认证错误，尝试刷新Token后重试");
                    tokenService.refreshToken(effectiveApiKey);
                    return callJiutianApiWithRetry(endpoint, request, agentApiKey, true);
                }
                
                throw new RuntimeException("API调用失败，状态码: " + response.getStatusCode() + ", 响应: " + responseBody);
            }
            
        } catch (Exception e) {
            // 检查是否是认证相关异常
            if (isAuthenticationException(e) && !isRetry) {
                log.info("检测到认证异常，尝试刷新Token后重试: {}", e.getMessage());
                try {
                    String effectiveApiKey = agentApiKey != null ? agentApiKey : apiKey;
                    tokenService.refreshToken(effectiveApiKey);
                    return callJiutianApiWithRetry(endpoint, request, agentApiKey, true);
                } catch (Exception refreshException) {
                    log.error("Token刷新失败", refreshException);
                }
            }
            
            log.error("Error calling Jiutian API", e);
            throw new RuntimeException("九天API调用异常: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否是认证错误
     */
    private boolean isAuthenticationError(HttpStatusCode statusCode, String responseBody) {
        // 检查HTTP状态码
        if (statusCode == HttpStatus.UNAUTHORIZED || statusCode == HttpStatus.FORBIDDEN) {
            return true;
        }
        
        // 检查响应体中的错误信息
        if (responseBody != null) {
            // 尝试解析JSON格式的错误响应
            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                // 检查九天平台特定的错误代码
                if (jsonNode.has("code")) {
                    int code = jsonNode.get("code").asInt();
                    // code 1001: 请求Header中未找到Authentication参数
                    // code 1002: Token无效或过期
                    if (code == 1001 || code == 1002) {
                        return true;
                    }
                }
                
                // 检查错误消息
                if (jsonNode.has("message")) {
                    String message = jsonNode.get("message").asText().toLowerCase();
                    if (message.contains("authentication") || 
                        message.contains("token") || 
                        message.contains("unauthorized") ||
                        message.contains("身份验证")) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // JSON解析失败，使用字符串匹配
                log.debug("Failed to parse response as JSON, using string matching", e);
            }
            
            // 字符串匹配检查
            String lowerBody = responseBody.toLowerCase();
            return lowerBody.contains("authentication") || 
                   lowerBody.contains("token") && (lowerBody.contains("expired") || lowerBody.contains("invalid")) ||
                   lowerBody.contains("unauthorized") ||
                   lowerBody.contains("请求header中未找到authentication参数") ||
                   lowerBody.contains("身份验证") ||
                   responseBody.contains("\"code\":1001") ||
                   responseBody.contains("\"code\":1002");
        }
        
        return false;
    }
    
    /**
     * 检查是否是认证相关异常
     */
    private boolean isAuthenticationException(Exception e) {
        String message = e.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("authentication") ||
                   lowerMessage.contains("unauthorized") ||
                   lowerMessage.contains("token") && (lowerMessage.contains("expired") || lowerMessage.contains("invalid")) ||
                   message.contains("请求Header中未找到Authentication参数");
        }
        return false;
    }

    /**
     * 解析九天平台API响应
     */
    private String parseResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            
            // 检查错误
            if (jsonNode.has("error")) {
                JsonNode errorNode = jsonNode.get("error");
                String errorMessage = errorNode.has("message") ? 
                        errorNode.get("message").asText() : "未知错误";
                throw new RuntimeException("API返回错误: " + errorMessage);
            }
            
            // 解析九天平台响应格式
            if (jsonNode.has("data")) {
                JsonNode dataNode = jsonNode.get("data");
                if (dataNode.has("content")) {
                    return dataNode.get("content").asText();
                }
            }
            
            // 备用解析方式
            if (jsonNode.has("content")) {
                return jsonNode.get("content").asText();
            }
            
            if (jsonNode.has("message")) {
                return jsonNode.get("message").asText();
            }
            
            throw new RuntimeException("无法解析API响应");
            
        } catch (Exception e) {
            log.error("Error parsing API response: {}", response, e);
            throw new RuntimeException("解析API响应失败: " + e.getMessage());
        }
    }

    /**
     * 创建HTTP头
     */
    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("User-Agent", "Lingxi-Chat/1.0");
        headers.set("Accept", "application/json");
        return headers;
    }

    /**
     * 将文本分割成块
     */
    private String[] splitIntoChunks(String text, int chunkSize) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }
        
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            chunks.add(text.substring(i, end));
        }
        
        return chunks.toArray(new String[0]);
    }

    /**
     * 流式响应处理器接口
     */
    public interface StreamResponseHandler {
        void onChunk(String chunk);
        void onComplete();
        void onError(Exception error);
    }

    /**
     * 默认流式响应处理器
     */
    public static class DefaultStreamResponseHandler implements StreamResponseHandler {
        private final StringBuilder content = new StringBuilder();
        
        @Override
        public void onChunk(String chunk) {
            content.append(chunk);
            log.debug("Received chunk: {}", chunk);
        }
        
        @Override
        public void onComplete() {
            log.info("Stream completed, total content length: {}", content.length());
        }
        
        @Override
        public void onError(Exception error) {
            log.error("Stream error", error);
        }
        
        public String getContent() {
            return content.toString();
        }
    }
}