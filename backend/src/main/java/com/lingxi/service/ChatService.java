package com.lingxi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingxi.entity.Agent;
import com.lingxi.entity.ChatHistory;
import com.lingxi.entity.User;
import com.lingxi.repository.AgentRepository;
import com.lingxi.repository.ChatHistoryRepository;
import com.lingxi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Consumer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 聊天服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final AgentRepository agentRepository;
    private final UserRepository userRepository;
    private final AgentService agentService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final JiutianTokenService jiutianTokenService;

    @Value("${app.jiutian.base-url}")
    private String jiutianApiUrl;

    @Value("${app.jiutian.api-key}")
    private String jiutianApiKey;

    @Value("${app.chat.max-history-length:20}")
    private int maxHistoryLength;

    @Value("${app.chat.session-timeout:3600}")
    private int sessionTimeoutSeconds;

    // @智能体名 的正则表达式
    private static final Pattern AGENT_MENTION_PATTERN = Pattern.compile("@([\u4e00-\u9fa5a-zA-Z0-9_-]+)");

    /**
     * 处理用户消息
     */
    @Transactional
    public ChatHistory processUserMessage(Long userId, String sessionId, String content, String metadata) {
        log.info("Processing user message from user: {} in session: {}", userId, sessionId);
        
        // 验证用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!user.getStatus().equals(User.UserStatus.ACTIVE)) {
            throw new RuntimeException("用户状态异常，无法发送消息");
        }
        
        // 创建用户消息记录
        ChatHistory userMessage = new ChatHistory();
        userMessage.setUser(user);
        userMessage.setSessionId(sessionId);
        userMessage.setMessageType(ChatHistory.MessageType.USER);
        userMessage.setContent(content);
        userMessage.setStatus(ChatHistory.MessageStatus.SUCCESS);
        userMessage.setMetadata(metadata);
        userMessage.setSequenceNumber(getNextSequenceNumber(sessionId));
        userMessage.setCreatedAt(LocalDateTime.now());
        
        userMessage = chatHistoryRepository.save(userMessage);
        
        // 解析消息中的@智能体
        List<String> mentionedAgents = extractMentionedAgents(content);
        
        if (!mentionedAgents.isEmpty()) {
            // 异步处理智能体响应
            processAgentResponses(userMessage, mentionedAgents);
        }
        
        return userMessage;
    }
    
    /**
     * 处理智能体响应
     */
    private void processAgentResponses(ChatHistory userMessage, List<String> mentionedAgents) {
        for (String agentName : mentionedAgents) {
            try {
                processAgentResponse(userMessage, agentName);
            } catch (Exception e) {
                log.error("Error processing agent response for agent: {}", agentName, e);
                // 创建错误响应记录
                createErrorResponse(userMessage, agentName, e.getMessage());
            }
        }
    }
    
    /**
     * 根据智能体ID处理智能体响应
     */
    @Transactional
    public ChatHistory processAgentResponseById(ChatHistory userMessage, Long agentId) {
        log.info("Processing agent response for agent ID: {} to message: {}", agentId, userMessage.getId());
        
        // 查找智能体
        Optional<Agent> agentOpt = agentRepository.findById(agentId);
        if (agentOpt.isEmpty()) {
            throw new RuntimeException("智能体不存在: " + agentId);
        }
        
        Agent agent = agentOpt.get();
        
        // 检查智能体状态
        if (!agent.getStatus().equals(Agent.AgentStatus.APPROVED) || !agent.getEnabled()) {
            throw new RuntimeException("智能体未启用或未审核通过: " + agent.getName());
        }
        
        return processAgentResponseInternal(userMessage, agent);
    }
    
    /**
     * 根据智能体ID处理智能体响应（支持流式回调）
     */
    @Transactional
    public ChatHistory processAgentResponseByIdWithCallback(ChatHistory userMessage, Long agentId, Consumer<String> chunkCallback) {
        log.info("Processing agent response with callback for agent ID: {} to message: {}", agentId, userMessage.getId());
        
        // 查找智能体
        Optional<Agent> agentOpt = agentRepository.findById(agentId);
        if (agentOpt.isEmpty()) {
            throw new RuntimeException("智能体不存在: " + agentId);
        }
        
        Agent agent = agentOpt.get();
        
        // 检查智能体状态
        if (!agent.getStatus().equals(Agent.AgentStatus.APPROVED) || !agent.getEnabled()) {
            throw new RuntimeException("智能体未启用或未审核通过: " + agent.getName());
        }
        
        return processAgentResponseInternalWithCallback(userMessage, agent, chunkCallback);
    }
    
    /**
     * 处理单个智能体响应
     */
    @Transactional
    public ChatHistory processAgentResponse(ChatHistory userMessage, String agentName) {
        log.info("Processing agent response for agent: {} to message: {}", agentName, userMessage.getId());
        
        // 查找智能体
        Optional<Agent> agentOpt = agentRepository.findByName(agentName);
        if (agentOpt.isEmpty()) {
            throw new RuntimeException("智能体不存在: " + agentName);
        }
        
        Agent agent = agentOpt.get();
        
        // 检查智能体状态
        if (!agent.getStatus().equals(Agent.AgentStatus.APPROVED) || !agent.getEnabled()) {
            throw new RuntimeException("智能体未启用或未审核通过: " + agentName);
        }
        
        return processAgentResponseInternal(userMessage, agent);
    }
    
    /**
     * 内部方法：处理智能体响应的通用逻辑
     */
    private ChatHistory processAgentResponseInternal(ChatHistory userMessage, Agent agent) {
        // 创建处理中的响应记录
        ChatHistory agentResponse = new ChatHistory();
        agentResponse.setUser(userMessage.getUser());
        agentResponse.setAgent(agent);
        agentResponse.setSessionId(userMessage.getSessionId());
        agentResponse.setMessageType(ChatHistory.MessageType.AGENT);
        agentResponse.setContent(userMessage.getContent());
        agentResponse.setStatus(ChatHistory.MessageStatus.PENDING);
        agentResponse.setParentMessageId(userMessage.getId());
        agentResponse.setSequenceNumber(getNextSequenceNumber(userMessage.getSessionId()));
        
        agentResponse = chatHistoryRepository.save(agentResponse);
        
        try {
            // 获取历史对话
            List<ChatHistory> history = getChatHistory(userMessage.getUserId(), userMessage.getSessionId(), agent.getId());
            
            // 调用九天API
            long startTime = System.currentTimeMillis();
            String response = callJiutianAPI(agent, userMessage.getContent(), history);
            long responseTime = System.currentTimeMillis() - startTime;


            
            // 更新响应记录
            agentResponse.setContent(response);
            agentResponse.setStatus(ChatHistory.MessageStatus.SUCCESS);
            agentResponse.setResponseTimeMs(responseTime);
            
            // 记录智能体调用统计
            agentService.recordAgentCall(agent.getId(), responseTime, true);
            return chatHistoryRepository.save(agentResponse);

        } catch (Exception e) {
            log.error("Error calling agent API: {}", agent.getName(), e);
            agentResponse.setStatus(ChatHistory.MessageStatus.FAILED);
            agentResponse.setErrorMessage(e.getMessage());
            
            // 记录失败调用
            agentService.recordAgentCall(agent.getId(), null, false);
            chatHistoryRepository.save(agentResponse); // 保存失败记录
            throw e; // 重新抛出异常，由Controller处理
        }
    }
    
    /**
     * 内部方法：处理智能体响应的通用逻辑（支持流式回调）
     */
    private ChatHistory processAgentResponseInternalWithCallback(ChatHistory userMessage, Agent agent, Consumer<String> chunkCallback) {
        // 创建处理中的响应记录
        ChatHistory agentResponse = new ChatHistory();
        agentResponse.setUser(userMessage.getUser());
        agentResponse.setAgent(agent);
        agentResponse.setSessionId(userMessage.getSessionId());
        agentResponse.setMessageType(ChatHistory.MessageType.AGENT);
        agentResponse.setContent(userMessage.getContent());
        agentResponse.setStatus(ChatHistory.MessageStatus.PENDING);
        agentResponse.setParentMessageId(userMessage.getId());
        agentResponse.setSequenceNumber(getNextSequenceNumber(userMessage.getSessionId()));
        
        agentResponse = chatHistoryRepository.save(agentResponse);
        
        try {
            // 获取历史对话
            List<ChatHistory> history = getChatHistory(userMessage.getUserId(), userMessage.getSessionId(), agent.getId());
            
            // 调用九天API（流式）
            long startTime = System.currentTimeMillis();
            String response = callJiutianAPIWithCallback(agent, userMessage.getContent(), history, chunkCallback);
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 更新响应记录
            agentResponse.setContent(response);
            agentResponse.setStatus(ChatHistory.MessageStatus.SUCCESS);
            agentResponse.setResponseTimeMs(responseTime);
            
            // 记录智能体调用统计
            agentService.recordAgentCall(agent.getId(), responseTime, true);
            return chatHistoryRepository.save(agentResponse);

        } catch (Exception e) {
            log.error("Error calling agent API with callback: {}", agent.getName(), e);
            agentResponse.setStatus(ChatHistory.MessageStatus.FAILED);
            agentResponse.setErrorMessage(e.getMessage());
            
            // 记录失败调用
            agentService.recordAgentCall(agent.getId(), null, false);
            chatHistoryRepository.save(agentResponse); // 保存失败记录
            throw e; // 重新抛出异常，由Controller处理
        }
    }
    
    /**
     * 调用九天API
     */
    private String callJiutianAPI(Agent agent, String userMessage, List<ChatHistory> history) {
        try {
            // 构建九天平台请求体格式 - 使用prompt格式
            Map<String, Object> requestBody = new HashMap<>();
            
            // 构建prompt字符串（包含历史对话）
            StringBuilder promptBuilder = new StringBuilder();
            
            // 添加历史对话
            for (ChatHistory chat : history) {
                if (chat.getMessageType() == ChatHistory.MessageType.USER) {
                    promptBuilder.append("用户: ").append(chat.getContent()).append("\n");
                } else if (chat.getMessageType() == ChatHistory.MessageType.AGENT && chat.getContent() != null) {
                    promptBuilder.append("助手: ").append(chat.getContent()).append("\n");
                }
            }
            
            // 添加当前用户消息
            promptBuilder.append("用户: ").append(userMessage).append("\n助手: ");
            
            requestBody.put("prompt", promptBuilder.toString());
            
            // 添加九天平台必需字段
            requestBody.put("appId", agent.getAppId());
            requestBody.put("stream", true); // 启用流式输出

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("Accept-Charset", "UTF-8");

            // 验证九天平台认证信息
            if (agent.getAppId() == null || agent.getAppId().trim().isEmpty()) {
                log.warn("智能体 {} 缺少appId配置", agent.getName());
                throw new RuntimeException("智能体配置不完整，请联系管理员。");
            }

            if (agent.getApiKey() == null || agent.getApiKey().trim().isEmpty()) {
                log.warn("智能体 {} 缺少apiKey配置", agent.getName());
                throw new RuntimeException("智能体配置不完整，请联系管理员。");
            }

            // 使用JiutianTokenService获取有效的Token（自动刷新）
            String validToken;
            try {
                validToken = jiutianTokenService.getValidToken(agent.getApiKey());
                log.debug("获取到有效Token for agent: {}", agent.getName());
            } catch (Exception e) {
                log.error("获取Token失败 for agent: {}", agent.getName(), e);
                throw new RuntimeException("Authentication Token已过期，请重新生成");
            }

            // 使用动态获取的token作为Bearer认证
            headers.setBearerAuth(validToken);

            // 添加API Key到请求头（九天平台可能需要）
            headers.set("X-API-Key", agent.getApiKey());
            headers.set("Authorization", "Bearer " + validToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 发送请求到九天平台 - 使用正确的API端点
            String endpoint = agent.getEndpoint() != null ? agent.getEndpoint() : "https://jiutian.10086.cn/largemodel/api/v2/completions";
            ResponseEntity<byte[]> response = restTemplate.postForEntity(endpoint, request, byte[].class);

            // 检查是否为认证错误，如果是则刷新Token并重试
            if (isAuthenticationError(response)) {
                log.warn("检测到认证错误，尝试刷新Token并重试 for agent: {}", agent.getName());
                try {
                    // 强制刷新Token
                    String refreshedToken = jiutianTokenService.refreshToken(agent.getApiKey());
                    
                    // 更新请求头中的Token
                    headers.setBearerAuth(refreshedToken);
                    headers.set("Authorization", "Bearer " + refreshedToken);
                    
                    // 重新创建请求并重试
                    HttpEntity<Map<String, Object>> retryRequest = new HttpEntity<>(requestBody, headers);
                    response = restTemplate.postForEntity(endpoint, retryRequest, byte[].class);
                    
                    log.info("Token刷新成功，重试请求成功 for agent: {}", agent.getName());
                } catch (Exception refreshException) {
                    log.error("Token刷新失败 for agent: {}", agent.getName(), refreshException);
                    throw new RuntimeException("Authentication Token已过期，请重新生成");
                }
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                // 正确处理UTF-8编码
                String responseBody = new String(response.getBody(), java.nio.charset.StandardCharsets.UTF_8);
                try {
                    Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

                    // 解析九天平台响应格式 - 处理choices数组
                    if (responseMap.containsKey("choices")) {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                        if (!choices.isEmpty()) {
                            Map<String, Object> firstChoice = choices.get(0);
                            if (firstChoice.containsKey("message")) {
                                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                                if (message.containsKey("content")) {
                                    return (String) message.get("content");
                                }
                            }
                            // 备用：直接从choice获取text字段
                            if (firstChoice.containsKey("text")) {
                                return (String) firstChoice.get("text");
                            }
                        }
                    }

                    // 解析九天平台响应格式 - 处理data字段
                    if (responseMap.containsKey("data")) {
                        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                        if (data.containsKey("content")) {
                            return (String) data.get("content");
                        }
                    }

                    // 备用解析方式
                    if (responseMap.containsKey("content")) {
                        return (String) responseMap.get("content");
                    }

                    if (responseMap.containsKey("message")) {
                        return (String) responseMap.get("message");
                    }

                    log.warn("九天平台响应内容为空或格式不正确: {}", responseBody);
                    throw new RuntimeException("智能体响应格式异常");

                } catch (Exception e) {
                    log.error("解析九天平台响应失败: {}", responseBody, e);
                    throw new RuntimeException("解析智能体响应失败: " + responseBody);
                }
            } else {
                log.error("九天API调用失败，状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("智能体暂时无法响应，请稍后再试。");
            }

        } catch (Exception e) {
            log.error("调用九天API时发生错误", e);
            throw new RuntimeException("智能体暂时无法响应，请稍后再试。", e);
        }
    }
    
    /**
     * 检查是否为认证错误
     */
    private boolean isAuthenticationError(ResponseEntity<byte[]> response) {
        // 检查HTTP状态码
        if (response.getStatusCode().value() == 401 || response.getStatusCode().value() == 403) {
            return true;
        }
        
        // 检查响应体中的错误信息
        if (response.getBody() != null) {
            try {
                String responseBody = new String(response.getBody(), java.nio.charset.StandardCharsets.UTF_8);
                String lowerCaseBody = responseBody.toLowerCase();
                
                // 检查九天平台特定的错误码
                if (responseBody.contains("\"code\":1001") || responseBody.contains("\"code\":1002")) {
                    return true;
                }
                
                // 检查常见的认证错误关键字
                return lowerCaseBody.contains("authentication") ||
                       lowerCaseBody.contains("token") ||
                       lowerCaseBody.contains("unauthorized") ||
                       lowerCaseBody.contains("已过期") ||
                       lowerCaseBody.contains("身份验证") ||
                       lowerCaseBody.contains("认证失败");
                       
            } catch (Exception e) {
                log.debug("解析响应体时出错，忽略认证错误检查", e);
            }
        }
        
        return false;
    }
    
    /**
     * 调用九天API（支持流式回调）
     */
    private String callJiutianAPIWithCallback(Agent agent, String userMessage, List<ChatHistory> history, Consumer<String> chunkCallback) {
        try {
            // 构建九天平台请求体格式 - 根据官方文档
            Map<String, Object> requestBody = new HashMap<>();
            
            // 构建history数组，格式为[["Q1","A1"], ["Q2","A2"]]
            List<List<String>> historyArray = new ArrayList<>();
            
            // 将历史对话转换为九天API要求的格式
            for (int i = 0; i < history.size(); i += 2) {
                if (i + 1 < history.size()) {
                    ChatHistory userChat = history.get(i);
                    ChatHistory agentChat = history.get(i + 1);
                    
                    if (userChat.getMessageType() == ChatHistory.MessageType.USER && 
                        agentChat.getMessageType() == ChatHistory.MessageType.AGENT) {
                        List<String> pair = new ArrayList<>();
                        pair.add(userChat.getContent());
                        pair.add(agentChat.getContent());
                        historyArray.add(pair);
                    }
                }
            }
            
            // 根据九天大模型文档构建请求体
            requestBody.put("modelId", "jiutian-lan");  // 使用九天蓝模型
            requestBody.put("prompt", userMessage);  // 当前用户输入
            requestBody.put("history", historyArray); // 历史对话数组格式
            requestBody.put("stream", true); // 启用流式输出
            
            // 添加params参数
            Map<String, Object> params = new HashMap<>();
            params.put("temperature", 0.8);
            params.put("top_p", 0.95);
            params.put("max_gen_len", 256);
            requestBody.put("params", params);

            // 验证九天平台认证信息
            if (agent.getApiKey() == null || agent.getApiKey().trim().isEmpty()) {
                log.warn("智能体 {} 缺少apiKey配置", agent.getName());
                throw new RuntimeException("智能体配置不完整，请联系管理员。");
            }

            // 使用JiutianTokenService获取有效的Token（自动刷新）
            String validToken;
            try {
                validToken = jiutianTokenService.getValidToken(agent.getApiKey());
                log.debug("获取到有效Token for agent: {}", agent.getName());
            } catch (Exception e) {
                log.error("获取Token失败 for agent: {}", agent.getName(), e);
                throw new RuntimeException("Authentication Token已过期，请重新生成");
            }

            // 构建请求URL - 使用智能体配置的endpoint
            String endpoint = agent.getEndpoint() != null ? agent.getEndpoint() : "https://jiutian.10086.cn/largemodel/api/v1/completions";
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // 设置请求方法和头部 - 根据九天文档
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setRequestProperty("Authorization", "Bearer " + validToken);
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);   // 5秒连接超时
            connection.setReadTimeout(180000);    // 3分钟读取超时（长对话）
            
            // 发送请求体
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            try (var outputStream = connection.getOutputStream()) {
                outputStream.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
            
            // 检查响应状态
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorResponse = "";
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse += line;
                    }
                }
                log.error("九天API调用失败，状态码: {}, 错误响应: {}", responseCode, errorResponse);
                throw new RuntimeException("智能体暂时无法响应，请稍后再试。");
            }
            
            // 读取流式响应
            StringBuilder fullResponse = new StringBuilder();
            int dataChunkCount = 0;
            boolean hasValidData = false;
            int lastSentLength = 0; // 用于跟踪已发送的内容长度，避免重复发送
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                int totalLineCount = 0;
                while ((line = reader.readLine()) != null) {
                    totalLineCount++;
                    log.debug("收到原始数据行 #{}: [{}]", totalLineCount, line);
                    
                    // 跳过空行
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    
                    String jsonData = null;
                    
                    // 处理标准SSE格式 (data: {...})
                    if (line.startsWith("data: ")) {
                        jsonData = line.substring(6); // 移除"data: "前缀
                        if ("[DONE]".equals(jsonData.trim())) {
                            log.info("收到SSE流结束标记");
                            break;
                        }
                    }
                    // 处理九天API的直接JSON格式
                    else if (line.startsWith("data:")) {
                        jsonData = line.substring(5); // 移除"data:"前缀
                        if ("[DONE]".equals(jsonData.trim())) {
                            log.info("收到九天流结束标记");
                            break;
                        }
                    }
                    // 处理纯JSON行（九天API的实际格式）
                    else if (line.startsWith("{") && line.endsWith("}")) {
                        jsonData = line;
                    }
                    
                    // 如果找到了JSON数据，进行解析
                    if (jsonData != null && !jsonData.trim().isEmpty()) {
                        dataChunkCount++;
                        log.debug("处理数据块 #{}: [{}]", dataChunkCount, jsonData.length() > 200 ? jsonData.substring(0, 200) + "..." : jsonData);
                        
                        try {
                            // 解析JSON数据
                            Map<String, Object> dataMap = objectMapper.readValue(jsonData, Map.class);
                            log.debug("解析到数据块 #{}", dataChunkCount);
                            
                            // 传递lastSentLength的引用来跟踪已发送长度
                            String content = extractContentFromStreamData(dataMap, fullResponse.length());
                            
                            if (content != null && !content.isEmpty()) {
                                hasValidData = true;
                                fullResponse.append(content);
                                log.debug("提取到内容: [{}]", content);
                                // 调用回调函数发送数据块
                                chunkCallback.accept(content);
                            } else {
                                log.debug("数据块 #{} 未提取到有效内容", dataChunkCount);
                            }
                        } catch (Exception e) {
                            log.warn("解析流式数据失败 (块 #{}): {}", dataChunkCount, e.getMessage());
                            // 继续处理下一行
                        }
                    } else {
                        log.debug("跳过非JSON行: [{}]", line.length() > 100 ? line.substring(0, 100) + "..." : line);
                    }
                }
                log.info("流式响应读取完成，总共读取 {} 行，其中 {} 个数据块", totalLineCount, dataChunkCount);
            }
            
            log.info("流式响应处理完成，共处理 {} 个数据块，是否有有效数据: {}", dataChunkCount, hasValidData);
            
            String result = fullResponse.toString();
            if (result.isEmpty()) {
                throw new RuntimeException("智能体响应为空");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("调用九天API流式接口时发生错误", e);
            throw new RuntimeException("智能体暂时无法响应，请稍后再试。", e);
        }
    }
    
    /**
     * 从流式数据中提取内容
     */
    private String extractContentFromStreamData(Map<String, Object> dataMap, int currentLength) {
        // 添加详细的调试日志
        log.info("提取内容 - 数据结构: {}", dataMap);
        
        // 根据九天平台流式响应格式解析
        // 格式: {"response":"累计内容","delta":"新增内容","finished":null/"Stop"}
        
        // 优先使用delta字段（新增内容）
        if (dataMap.containsKey("delta")) {
            Object deltaObj = dataMap.get("delta");
            log.info("发现delta字段，类型: {}, 值: {}", deltaObj != null ? deltaObj.getClass().getSimpleName() : "null", deltaObj);
            
            if (deltaObj instanceof String) {
                String delta = (String) deltaObj;
                // 过滤掉结束标记
                if (delta != null && !"[EOS]".equals(delta) && !delta.trim().isEmpty()) {
                    log.info("从delta字段提取到内容: [{}]", delta);
                    return delta;
                }
            }
        }
        
        // 如果没有delta或delta为空，尝试使用response字段（累计内容）
        // 需要计算增量内容，避免重复发送
        if (dataMap.containsKey("response")) {
            Object responseObj = dataMap.get("response");
            log.info("发现response字段，类型: {}, 值: {}", responseObj != null ? responseObj.getClass().getSimpleName() : "null", responseObj);
            
            if (responseObj instanceof String) {
                String response = (String) responseObj;
                if (response != null && !response.trim().isEmpty()) {
                    // 计算新增的内容（避免重复发送累计内容）
                    if (response.length() > currentLength) {
                        String newContent = response.substring(currentLength);
                        log.info("从response字段提取到新增内容: [{}]，当前累计长度: {}", newContent, response.length());
                        return newContent;
                    } else {
                        log.debug("response字段内容未增加，跳过发送");
                        return null;
                    }
                }
            }
        }
        
        // 兼容其他可能的格式
        if (dataMap.containsKey("choices")) {
            Object choicesObj = dataMap.get("choices");
            log.info("发现choices字段，类型: {}", choicesObj != null ? choicesObj.getClass().getSimpleName() : "null");
            
            if (choicesObj instanceof List) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) choicesObj;
                if (!choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    log.info("第一个choice结构: {}", firstChoice);
                    
                    if (firstChoice.containsKey("delta")) {
                         Object deltaObj = firstChoice.get("delta");
                         log.info("choice中的delta字段，类型: {}, 值: {}", deltaObj != null ? deltaObj.getClass().getSimpleName() : "null", deltaObj);
                         
                         if (deltaObj instanceof Map) {
                             Map<String, Object> delta = (Map<String, Object>) deltaObj;
                             
                             // 检查是否有content字段
                             if (delta.containsKey("content")) {
                                 Object contentObj = delta.get("content");
                                 if (contentObj instanceof String) {
                                     String content = (String) contentObj;
                                     log.info("从choices.delta.content提取到内容: [{}]", content);
                                     return content;
                                 }
                             }
                             
                             // 检查是否有tool_calls字段（九天API的实际格式）
                             if (delta.containsKey("tool_calls")) {
                                 Object toolCallsObj = delta.get("tool_calls");
                                 log.info("发现tool_calls字段，类型: {}", toolCallsObj != null ? toolCallsObj.getClass().getSimpleName() : "null");
                                 
                                 if (toolCallsObj instanceof List) {
                                     List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) toolCallsObj;
                                     for (Map<String, Object> toolCall : toolCalls) {
                                         log.info("处理tool_call: role={}, type={}", toolCall.get("role"), toolCall.get("type"));
                                         
                                         // 检查type为text的工具调用（这是实际的文本内容）
                                         if ("text".equals(toolCall.get("type"))) {
                                             // 直接从text字段获取内容
                                             Object textObj = toolCall.get("text");
                                             if (textObj instanceof String) {
                                                 String text = (String) textObj;
                                                 log.info("从tool_calls.text提取到内容: [{}]", text);
                                                 return text;
                                             }
                                         }
                                         
                                         // 检查role为memory的工具调用（可能包含其他信息）
                                         if ("memory".equals(toolCall.get("role"))) {
                                             Object contentObj = toolCall.get("content");
                                             if (contentObj instanceof Map) {
                                                 Map<String, Object> contentMap = (Map<String, Object>) contentObj;
                                                 if (contentMap.containsKey("output")) {
                                                     Object outputObj = contentMap.get("output");
                                                     if (outputObj instanceof String) {
                                                         String output = (String) outputObj;
                                                         log.info("从tool_calls.content.output提取到内容: [{}]", output);
                                                         return output;
                                                     }
                                                 }
                                             }
                                         }
                                         
                                         // 检查role为assistant的工具调用（保持向后兼容）
                                         if ("assistant".equals(toolCall.get("role"))) {
                                             Object contentObj = toolCall.get("content");
                                             if (contentObj instanceof Map) {
                                                 Map<String, Object> contentMap = (Map<String, Object>) contentObj;
                                                 if (contentMap.containsKey("output")) {
                                                     Object outputObj = contentMap.get("output");
                                                     if (outputObj instanceof String) {
                                                         String output = (String) outputObj;
                                                         log.info("从tool_calls.content.output提取到内容: [{}]", output);
                                                         return output;
                                                     }
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                    if (firstChoice.containsKey("text")) {
                        Object textObj = firstChoice.get("text");
                        if (textObj instanceof String) {
                            String text = (String) textObj;
                            log.info("从choices.text提取到内容: [{}]", text);
                            return text;
                        }
                    }
                }
            }
        }
        
        // 备用解析方式
        if (dataMap.containsKey("content")) {
            Object contentObj = dataMap.get("content");
            if (contentObj instanceof String) {
                String content = (String) contentObj;
                log.info("从content字段提取到内容: [{}]", content);
                return content;
            }
        }
        
        if (dataMap.containsKey("text")) {
            Object textObj = dataMap.get("text");
            if (textObj instanceof String) {
                String text = (String) textObj;
                log.info("从text字段提取到内容: [{}]", text);
                return text;
            }
        }
        
        log.warn("未能从数据中提取到任何内容，数据键: {}", dataMap.keySet());
        return null;
    }

    /**
     * 构建消息列表
     */
    private List<Map<String, Object>> buildMessages(String userMessage, List<ChatHistory> history) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 添加历史消息
        for (ChatHistory chat : history) {
            if (chat.getMessageType() == ChatHistory.MessageType.USER) {
                Map<String, Object> message = new HashMap<>();
                message.put("role", "user");
                message.put("content", chat.getContent());
                messages.add(message);
            } else if (chat.getMessageType() == ChatHistory.MessageType.AGENT && chat.getContent() != null) {
                Map<String, Object> message = new HashMap<>();
                message.put("role", "assistant");
                message.put("content", chat.getContent());
                messages.add(message);
            }
        }
        
        // 添加当前用户消息
        Map<String, Object> currentMessage = new HashMap<>();
        currentMessage.put("role", "user");
        currentMessage.put("content", userMessage);
        messages.add(currentMessage);
        
        return messages;
    }
    
    /**
     * 获取聊天历史
     */
    @Transactional(readOnly = true)
    public List<ChatHistory> getChatHistory(Long userId, String sessionId, Long agentId) {
        List<ChatHistory> history;
        
        if (agentId != null) {
            history = chatHistoryRepository.findByUser_IdAndAgent_IdAndSessionIdOrderBySequenceNumberAsc(userId, agentId, sessionId);
        } else {
            history = chatHistoryRepository.findByUser_IdAndSessionIdOrderBySequenceNumberAsc(userId, sessionId);
        }
        
        // 在事务内访问懒加载属性，确保代理对象被初始化
        for (ChatHistory chat : history) {
            if (chat.getUser() != null) {
                // 触发懒加载
                chat.getUser().getUsername();
            }
            if (chat.getAgent() != null) {
                // 触发懒加载
                chat.getAgent().getName();
            }
        }
        
        // 限制历史长度
        if (history.size() > maxHistoryLength) {
            history = history.subList(history.size() - maxHistoryLength, history.size());
        }
        
        return history;
    }
    
    /**
     * 获取用户会话列表
     */
    public List<String> getUserSessions(Long userId, int limit) {
        Page<String> page = chatHistoryRepository.findRecentSessionsByUser(userId, 
                org.springframework.data.domain.PageRequest.of(0, limit));
        return page.getContent();
    }

    /**
     * 获取会话中的最后一条消息
     */
    public Optional<ChatHistory> getLastMessage(String sessionId) {
        List<ChatHistory> messages = chatHistoryRepository.findLastMessageInSession(sessionId, 
                org.springframework.data.domain.PageRequest.of(0, 1));
        return messages.isEmpty() ? Optional.empty() : Optional.of(messages.get(0));
    }
    
    /**
     * 删除会话
     */
    @Transactional
    public void deleteSession(Long userId, String sessionId) {
        log.info("Deleting session: {} for user: {}", sessionId, userId);
        
        // 验证会话属于该用户
        List<ChatHistory> sessionMessages = chatHistoryRepository.findByUser_IdAndSessionIdOrderBySequenceNumberAsc(userId, sessionId);
        if (sessionMessages.isEmpty()) {
            throw new RuntimeException("会话不存在或无权限删除");
        }
        
        chatHistoryRepository.deleteChatHistoryBySessionId(sessionId);
    }
    
    /**
     * 清空用户聊天历史
     */
    @Transactional
    public void clearUserChatHistory(Long userId) {
        log.info("Clearing chat history for user: {}", userId);
        chatHistoryRepository.deleteChatHistoryByUserId(userId);
    }



    /**
     * 删除单个消息
     */
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        ChatHistory message = chatHistoryRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        
        // 验证权限
        if (!message.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限删除此消息");
        }
        
        chatHistoryRepository.deleteById(messageId);
        log.info("Message deleted: {} by user: {}", messageId, userId);
    }
    
    /**
     * 提取消息中提到的智能体
     */
    private List<String> extractMentionedAgents(String content) {
        List<String> agents = new ArrayList<>();
        Matcher matcher = AGENT_MENTION_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String agentName = matcher.group(1);
            if (!agents.contains(agentName)) {
                agents.add(agentName);
            }
        }
        
        return agents;
    }
    
    /**
     * 获取下一个序列号
     */
    private Integer getNextSequenceNumber(String sessionId) {
        List<ChatHistory> sessionMessages = chatHistoryRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId);
        if (sessionMessages.isEmpty()) {
            return 1;
        }
        return sessionMessages.get(sessionMessages.size() - 1).getSequenceNumber() + 1;
    }
    
    /**
     * 创建错误响应
     */
    private ChatHistory createErrorResponse(ChatHistory userMessage, String agentName, String errorMessage) {
        ChatHistory errorResponse = new ChatHistory();
        errorResponse.setUser(userMessage.getUser());
        errorResponse.setSessionId(userMessage.getSessionId());
        errorResponse.setMessageType(ChatHistory.MessageType.SYSTEM);
        errorResponse.setContent("调用智能体 @" + agentName + " 失败: " + errorMessage);
        errorResponse.setStatus(ChatHistory.MessageStatus.FAILED);
        errorResponse.setErrorMessage(errorMessage);
        errorResponse.setParentMessageId(userMessage.getId());
        errorResponse.setSequenceNumber(getNextSequenceNumber(userMessage.getSessionId()));
        
        return chatHistoryRepository.save(errorResponse);
    }
    
    /**
     * 评价消息
     */
    @Transactional
    public void rateMessage(Long messageId, Integer rating, Boolean helpful, Long userId) {
        ChatHistory message = chatHistoryRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        
        // 验证权限
        if (!message.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限评价此消息");
        }
        
        message.setUserRating(rating);
        message.setIsHelpful(helpful);
        
        chatHistoryRepository.save(message);
        
        // 更新智能体评分
        if (message.getAgent() != null && rating != null) {
            agentService.updateAgentRating(message.getAgent().getId(), rating.doubleValue());
        }
    }
    
    /**
     * 搜索聊天历史
     */
    public Page<ChatHistory> searchChatHistory(Long userId, String keyword, Pageable pageable) {
        return chatHistoryRepository.findByUser_IdAndKeyword(userId, keyword, pageable);
    }
    
    /**
     * 获取消息统计
     */
    public Optional<ChatHistory> getMessageById(Long messageId) {
        return chatHistoryRepository.findById(messageId);
    }

    public Page<ChatHistory> getFailedMessages(Long userId, Pageable pageable) {
        return chatHistoryRepository.findByUser_IdAndStatusIn(userId, 
                List.of(ChatHistory.MessageStatus.FAILED, ChatHistory.MessageStatus.TIMEOUT), 
                pageable);
    }

    @Transactional
    public ChatHistory retryFailedMessage(Long messageId, Long userId) {
        ChatHistory message = chatHistoryRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        
        // 验证权限
        if (!message.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限重试此消息");
        }
        
        // 验证消息状态
        if (!message.isFailed()) {
            throw new RuntimeException("只能重试失败的消息");
        }
        
        // 重新处理消息
        return processUserMessage(userId, message.getSessionId(), 
                message.getRawContent() != null ? message.getRawContent() : message.getContent(), 
                message.getMetadata());
    }

    public Map<String, Object> getChatStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总消息数
        long totalMessages = chatHistoryRepository.countByUserId(userId);
        stats.put("totalMessages", totalMessages);
        
        // 今日消息数
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayMessages = chatHistoryRepository.countTodayMessagesByUser(userId, startOfDay);
        stats.put("todayMessages", todayMessages);
        
        // 最近会话数
        List<String> recentSessions = getUserSessions(userId, 10);
        stats.put("recentSessionCount", recentSessions.size());
        
        return stats;
    }
    
    /**
     * 清理过期会话
     */
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(sessionTimeoutSeconds);
        int deletedCount = chatHistoryRepository.deleteOldChatHistory(cutoffTime);
        log.info("Cleaned up {} expired chat messages", deletedCount);
    }
}