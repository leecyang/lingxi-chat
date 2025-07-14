package com.lingxi.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingxi.entity.ChatHistory;
import com.lingxi.service.ChatService;
import com.lingxi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket聊天处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    // 存储用户会话
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        
        try {
            // 从查询参数中获取token
            String token = extractTokenFromSession(session);
            if (token == null || !jwtUtil.validateToken(token)) {
                log.warn("Invalid token for WebSocket connection: {}", session.getId());
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
                return;
            }
            
            // 获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                log.warn("Cannot extract user ID from token for session: {}", session.getId());
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid user"));
                return;
            }
            
            // 存储会话映射
            userSessions.put(session.getId(), session);
            sessionUserMap.put(session.getId(), userId);
            
            log.info("User {} connected via WebSocket session: {}", userId, session.getId());
            
            // 发送连接成功消息
            sendMessage(session, createSystemMessage("connected", "连接成功", null));
            
        } catch (Exception e) {
            log.error("Error establishing WebSocket connection: {}", session.getId(), e);
            session.close(CloseStatus.SERVER_ERROR.withReason("Connection error"));
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (!(message instanceof TextMessage)) {
            return;
        }
        
        String payload = ((TextMessage) message).getPayload();
        log.debug("Received WebSocket message from session {}: {}", session.getId(), payload);
        
        try {
            // 解析消息
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            String messageType = (String) messageData.get("type");
            
            Long userId = sessionUserMap.get(session.getId());
            if (userId == null) {
                sendError(session, "用户未认证");
                return;
            }
            
            switch (messageType) {
                case "chat":
                    handleChatMessage(session, userId, messageData);
                    break;
                case "ping":
                    handlePingMessage(session);
                    break;
                case "join_session":
                    handleJoinSession(session, userId, messageData);
                    break;
                case "leave_session":
                    handleLeaveSession(session, userId, messageData);
                    break;
                case "rate_message":
                    handleRateMessage(session, userId, messageData);
                    break;
                default:
                    log.warn("Unknown message type: {} from session: {}", messageType, session.getId());
                    sendError(session, "未知的消息类型");
            }
            
        } catch (Exception e) {
            log.error("Error handling WebSocket message from session: {}", session.getId(), e);
            sendError(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), closeStatus);
        
        // 清理会话映射
        userSessions.remove(session.getId());
        sessionUserMap.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 处理聊天消息
     */
    private void handleChatMessage(WebSocketSession session, Long userId, Map<String, Object> messageData) {
        try {
            String content = (String) messageData.get("content");
            String sessionId = (String) messageData.get("sessionId");
            String metadata = (String) messageData.get("metadata");
            
            if (content == null || content.trim().isEmpty()) {
                sendError(session, "消息内容不能为空");
                return;
            }
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sendError(session, "会话ID不能为空");
                return;
            }
            
            // 处理用户消息
            ChatHistory userMessage = chatService.processUserMessage(userId, sessionId, content, metadata);
            
            // 发送用户消息确认
            sendMessage(session, createChatMessage("user_message", userMessage));
            
            // 智能体响应会通过ChatService异步处理，这里可以发送处理中状态
            sendMessage(session, createSystemMessage("processing", "正在处理智能体响应...", sessionId));
            
        } catch (Exception e) {
            log.error("Error handling chat message from user: {}", userId, e);
            sendError(session, "发送消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理ping消息
     */
    private void handlePingMessage(WebSocketSession session) {
        try {
            sendMessage(session, createSystemMessage("pong", "pong", null));
        } catch (Exception e) {
            log.error("Error handling ping message", e);
        }
    }
    
    /**
     * 处理加入会话
     */
    private void handleJoinSession(WebSocketSession session, Long userId, Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sendError(session, "会话ID不能为空");
                return;
            }
            
            // 获取会话历史
            var history = chatService.getChatHistory(userId, sessionId, null);
            
            sendMessage(session, Map.of(
                "type", "session_joined",
                "sessionId", sessionId,
                "history", history,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Error joining session for user: {}", userId, e);
            sendError(session, "加入会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理离开会话
     */
    private void handleLeaveSession(WebSocketSession session, Long userId, Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            
            sendMessage(session, createSystemMessage("session_left", "已离开会话", sessionId));
            
        } catch (Exception e) {
            log.error("Error leaving session for user: {}", userId, e);
            sendError(session, "离开会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理消息评分
     */
    private void handleRateMessage(WebSocketSession session, Long userId, Map<String, Object> messageData) {
        try {
            Long messageId = Long.valueOf(messageData.get("messageId").toString());
            Integer rating = messageData.get("rating") != null ? 
                Integer.valueOf(messageData.get("rating").toString()) : null;
            Boolean helpful = (Boolean) messageData.get("helpful");
            
            chatService.rateMessage(messageId, rating, helpful, userId);
            
            sendMessage(session, createSystemMessage("message_rated", "评分成功", null));
            
        } catch (Exception e) {
            log.error("Error rating message for user: {}", userId, e);
            sendError(session, "评分失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送智能体响应（供ChatService调用）
     */
    public void sendAgentResponse(Long userId, ChatHistory agentResponse) {
        // 查找用户的WebSocket会话
        for (Map.Entry<String, Long> entry : sessionUserMap.entrySet()) {
            if (entry.getValue().equals(userId)) {
                WebSocketSession session = userSessions.get(entry.getKey());
                if (session != null && session.isOpen()) {
                    try {
                        sendMessage(session, createChatMessage("agent_response", agentResponse));
                    } catch (Exception e) {
                        log.error("Error sending agent response to user: {}", userId, e);
                    }
                }
            }
        }
    }
    
    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, Object message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }
    
    /**
     * 发送错误消息
     */
    private void sendError(WebSocketSession session, String error) {
        try {
            sendMessage(session, Map.of(
                "type", "error",
                "message", error,
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Error sending error message", e);
        }
    }
    
    /**
     * 创建系统消息
     */
    private Map<String, Object> createSystemMessage(String type, String message, String sessionId) {
        Map<String, Object> msg = Map.of(
            "type", type,
            "message", message,
            "timestamp", System.currentTimeMillis()
        );
        
        if (sessionId != null) {
            msg = new java.util.HashMap<>(msg);
            msg.put("sessionId", sessionId);
        }
        
        return msg;
    }
    
    /**
     * 创建聊天消息
     */
    private Map<String, Object> createChatMessage(String type, ChatHistory chatHistory) {
        return Map.of(
            "type", type,
            "data", chatHistory,
            "timestamp", System.currentTimeMillis()
        );
    }
    
    /**
     * 从WebSocket会话中提取token
     */
    private String extractTokenFromSession(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri != null) {
                String query = uri.getQuery();
                if (query != null) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                            return keyValue[1];
                        }
                    }
                }
            }
            
            // 尝试从headers中获取
            var headers = session.getHandshakeHeaders();
            var authHeaders = headers.get("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.get(0);
                if (authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                }
            }
            
        } catch (Exception e) {
            log.error("Error extracting token from WebSocket session", e);
        }
        
        return null;
    }
    
    /**
     * 广播消息给所有在线用户
     */
    public void broadcastMessage(Object message) {
        for (WebSocketSession session : userSessions.values()) {
            if (session.isOpen()) {
                try {
                    sendMessage(session, message);
                } catch (Exception e) {
                    log.error("Error broadcasting message to session: {}", session.getId(), e);
                }
            }
        }
    }
    
    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return (int) userSessions.values().stream()
                .filter(WebSocketSession::isOpen)
                .count();
    }
    
    /**
     * 获取用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return sessionUserMap.containsValue(userId) && 
               sessionUserMap.entrySet().stream()
                   .anyMatch(entry -> entry.getValue().equals(userId) && 
                            userSessions.get(entry.getKey()).isOpen());
    }
}