package com.lingxi.controller;

import com.lingxi.entity.ChatHistory;
import com.lingxi.dto.ChatHistoryDTO;
import com.lingxi.service.ChatService;
import com.lingxi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageImpl;
import java.util.ArrayList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentication Token已过期，请重新生成"));
            }
            
            String content = (String) request.get("content");
            String sessionId = (String) request.get("sessionId");
            Long agentId = request.get("agentId") != null ? 
                    Long.valueOf(request.get("agentId").toString()) : null;
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "消息内容不能为空"));
            }
            
            // 处理用户消息
            ChatHistory userMessage = chatService.processUserMessage(
                    userId, sessionId, content, null);
            
            // 如果指定了智能体ID，直接调用该智能体生成回复
            if (agentId != null) {
                try {
                    ChatHistory agentResponse = chatService.processAgentResponseById(
                            userMessage, agentId);
                    log.info("Agent {} responded to user {} message", agentId, userId);
                    return ResponseEntity.ok(Map.of(
                            "message", "消息发送成功",
                            "chatHistory", agentResponse
                    ));
                } catch (Exception e) {
                    log.error("Error processing agent response for agent: {}", agentId, e);
                    // 如果智能体回复失败，返回错误信息
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "智能体回复失败: " + e.getMessage()));
                }
            }
            
            log.info("Message sent by user: {} without specific agent", userId);
            return ResponseEntity.ok(Map.of(
                    "message", "消息已接收，但未指定智能体",
                    "chatHistory", userMessage
            ));
            
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 流式发送消息 (Server-Sent Events)
     */
    @PostMapping(value = "/send/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        
        SseEmitter emitter = new SseEmitter(30000L); // 30秒超时
        
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                // 认证失败时应该返回401状态码而不是SSE事件
                throw new RuntimeException("Authentication Token已过期，请重新生成");
            }
            
            String content = (String) request.get("content");
            String sessionId = (String) request.get("sessionId");
            Long agentId = request.get("agentId") != null ? 
                    Long.valueOf(request.get("agentId").toString()) : null;
            
            if (content == null || content.trim().isEmpty()) {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data(Map.of("error", "消息内容不能为空")));
                emitter.complete();
                return emitter;
            }
            
            // 异步处理消息
            CompletableFuture.runAsync(() -> {
                try {
                    // 处理用户消息
                    ChatHistory userMessage = chatService.processUserMessage(
                            userId, sessionId, content, null);
                    
                    // 发送用户消息确认
                    emitter.send(SseEmitter.event()
                        .name("user_message")
                        .data(Map.of(
                            "message", "用户消息已接收",
                            "chatHistory", userMessage
                        )));
                    
                    // 如果指定了智能体ID，调用智能体生成流式回复
                    if (agentId != null) {
                        // 发送开始生成事件
                        emitter.send(SseEmitter.event()
                            .name("agent_start")
                            .data(Map.of("message", "智能体开始生成回复...")));
                        
                        try {
                            // 这里需要修改ChatService支持流式回调
                            ChatHistory agentResponse = chatService.processAgentResponseByIdWithCallback(
                                userMessage, agentId, (chunk) -> {
                                    try {
                                        // 发送流式数据块 - 使用标准SSE格式，指定事件名称
                                        emitter.send(SseEmitter.event()
                                            .name("chunk")
                                            .data(Map.of("chunk", chunk)));
                                        log.debug("发送SSE数据块: {}", chunk);
                                    } catch (IOException e) {
                                        log.error("Error sending SSE chunk", e);
                                    }
                                });
                            
                            // 发送完成事件
                            emitter.send(SseEmitter.event()
                                .name("agent_complete")
                                .data(Map.of(
                                    "message", "智能体回复完成",
                                    "chatHistory", agentResponse
                                )));
                            
                            log.info("Agent {} responded to user {} message via stream", agentId, userId);
                            
                        } catch (Exception e) {
                            log.error("Error processing agent response for agent: {}", agentId, e);
                            emitter.send(SseEmitter.event()
                                .name("error")
                                .data(Map.of("error", "智能体回复失败: " + e.getMessage())));
                        }
                    } else {
                        emitter.send(SseEmitter.event()
                            .name("info")
                            .data(Map.of("message", "消息已接收，但未指定智能体")));
                    }
                    
                    emitter.complete();
                    
                } catch (Exception e) {
                    log.error("Error in stream processing", e);
                    try {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("error", e.getMessage())));
                        emitter.complete();
                    } catch (IOException ioException) {
                        log.error("Error sending error event", ioException);
                    }
                }
            });
            
        } catch (Exception e) {
            log.error("Error setting up stream", e);
            // 如果是认证错误，直接抛出异常让Spring处理
            if (e.getMessage() != null && e.getMessage().contains("Authentication Token已过期")) {
                throw new RuntimeException(e.getMessage());
            }
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data(Map.of("error", e.getMessage())));
                emitter.complete();
            } catch (IOException ioException) {
                log.error("Error sending initial error event", ioException);
            }
        }
        
        return emitter;
    }

    /**
     * 获取聊天历史
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getChatHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) Long agentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentication Token已过期，请重新生成"));
            }
            
            Pageable pageable = PageRequest.of(page, size, 
                    Sort.by(Sort.Direction.DESC, "createdAt"));
            
            // 获取聊天历史（不分页，因为ChatService.getChatHistory不支持分页）
            List<ChatHistory> historyList = chatService.getChatHistory(userId, sessionId, agentId);
            
            // 手动实现分页
            int start = page * size;
            int end = Math.min(start + size, historyList.size());
            List<ChatHistory> pagedHistory = start < historyList.size() ? 
                historyList.subList(start, end) : new ArrayList<>();
            
            // 转换为DTO
            List<ChatHistoryDTO> historyDTOs = pagedHistory.stream()
                    .map(ChatHistoryDTO::fromEntity)
                    .collect(Collectors.toList());
            
            // 创建分页对象
            Page<ChatHistoryDTO> historyPage = new PageImpl<>(historyDTOs, pageable, historyList.size());
            
            return ResponseEntity.ok(Map.of(
                    "history", historyPage.getContent(),
                    "total", historyPage.getTotalElements(),
                    "totalPages", historyPage.getTotalPages(),
                    "currentPage", historyPage.getNumber(),
                    "size", historyPage.getSize()
            ));
            
        } catch (Exception e) {
            log.error("Error getting chat history", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取用户的会话列表
     */
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getUserSessions(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentication Token已过期，请重新生成"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            List<String> sessions = chatService.getUserSessions(userId, size);
            
            return ResponseEntity.ok(Map.of(
                    "sessions", sessions,
                    "total", sessions.size()
            ));
            
        } catch (Exception e) {
            log.error("Error getting user sessions", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取会话的最后一条消息
     */
    @GetMapping("/sessions/{sessionId}/last")
    public ResponseEntity<Map<String, Object>> getLastMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String sessionId) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentication Token已过期，请重新生成"));
            }
            
            Optional<ChatHistory> lastMessage = chatService.getLastMessage(sessionId);
            
            if (lastMessage.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ChatHistoryDTO messageDTO = ChatHistoryDTO.fromEntity(lastMessage.get());
            return ResponseEntity.ok(Map.of("message", messageDTO));
            
        } catch (Exception e) {
            log.error("Error getting last message for session: {}", sessionId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 评价消息
     */
    @PostMapping("/rate")
    public ResponseEntity<Map<String, String>> rateMessage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            Long messageId = Long.valueOf(request.get("messageId").toString());
            Integer rating = Integer.valueOf(request.get("rating").toString());
            String feedback = (String) request.get("feedback");
            
            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "评分必须在1-5之间"));
            }
            
            chatService.rateMessage(messageId, rating, null, userId);
            
            log.info("Message rated: {} with rating: {} by user: {}", messageId, rating, userId);
            return ResponseEntity.ok(Map.of("message", "评价成功"));
            
        } catch (Exception e) {
            log.error("Error rating message", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 标记消息为有用/无用
     */
    @PostMapping("/helpful")
    public ResponseEntity<Map<String, String>> markHelpful(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            Long messageId = Long.valueOf(request.get("messageId").toString());
            Boolean helpful = Boolean.valueOf(request.get("helpful").toString());
            
            chatService.rateMessage(messageId, null, helpful, userId);
            
            log.info("Message marked as {}: {} by user: {}", 
                    helpful ? "helpful" : "not helpful", messageId, userId);
            return ResponseEntity.ok(Map.of("message", 
                    helpful ? "标记为有用" : "标记为无用"));
            
        } catch (Exception e) {
            log.error("Error marking message helpful", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 删除聊天历史
     */
    @DeleteMapping("/history/{messageId}")
    public ResponseEntity<Map<String, String>> deleteMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long messageId) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            chatService.deleteMessage(messageId, userId);
            
            log.info("Message deleted: {} by user: {}", messageId, userId);
            return ResponseEntity.ok(Map.of("message", "消息删除成功"));
            
        } catch (Exception e) {
            log.error("Error deleting message: {}", messageId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 删除会话历史
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, String>> deleteSession(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String sessionId) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            chatService.deleteSession(userId, sessionId);
            
            log.info("Session deleted: {} by user: {}", sessionId, userId);
            return ResponseEntity.ok(Map.of("message", "会话删除成功"));
            
        } catch (Exception e) {
            log.error("Error deleting session: {}", sessionId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 清空用户聊天历史
     */
    @DeleteMapping("/history/clear")
    public ResponseEntity<Map<String, String>> clearHistory(
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            chatService.clearUserChatHistory(userId);
            
            log.info("Chat history cleared for user: {}", userId);
            return ResponseEntity.ok(Map.of("message", "聊天历史清空成功"));
            
        } catch (Exception e) {
            log.error("Error clearing chat history", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 搜索聊天历史
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentication Token已过期，请重新生成"));
            }
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "搜索关键词不能为空"));
            }
            
            Pageable pageable = PageRequest.of(page, size, 
                    Sort.by(Sort.Direction.DESC, "createdAt"));
            
            Page<ChatHistory> historyPage = chatService.searchChatHistory(
                    userId, keyword, pageable);
            
            // 转换为DTO
            List<ChatHistoryDTO> historyDTOs = historyPage.getContent().stream()
                    .map(ChatHistoryDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "history", historyDTOs,
                    "total", historyPage.getTotalElements(),
                    "totalPages", historyPage.getTotalPages(),
                    "currentPage", historyPage.getNumber(),
                    "size", historyPage.getSize(),
                    "keyword", keyword
            ));
            
        } catch (Exception e) {
            log.error("Error searching chat history", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取聊天统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getChatStats(
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentication Token已过期，请重新生成"));
            }
            
            Map<String, Object> stats = chatService.getChatStatistics(userId);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting chat stats", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取失败的消息
     */
    @GetMapping("/failed")
    public ResponseEntity<Map<String, Object>> getFailedMessages(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentication Token已过期，请重新生成"));
            }
            
            Pageable pageable = PageRequest.of(page, size, 
                    Sort.by(Sort.Direction.DESC, "createdAt"));
            
            Page<ChatHistory> failedMessages = chatService.getFailedMessages(userId, pageable);
            
            // 转换为DTO
            List<ChatHistoryDTO> messageDTOs = failedMessages.getContent().stream()
                    .map(ChatHistoryDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "messages", messageDTOs,
                    "total", failedMessages.getTotalElements(),
                    "totalPages", failedMessages.getTotalPages(),
                    "currentPage", failedMessages.getNumber(),
                    "size", failedMessages.getSize()
            ));
            
        } catch (Exception e) {
            log.error("Error getting failed messages", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 重试失败的消息
     */
    @PostMapping("/retry/{messageId}")
    public ResponseEntity<Map<String, Object>> retryMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long messageId) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentication Token已过期，请重新生成"));
            }
            
            ChatHistory retriedMessage = chatService.retryFailedMessage(messageId, userId);
            
            // 转换为DTO
            ChatHistoryDTO messageDTO = ChatHistoryDTO.fromEntity(retriedMessage);
            
            log.info("Message retried: {} by user: {}", messageId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "消息重试成功",
                    "chatHistory", messageDTO
            ));
            
        } catch (Exception e) {
            log.error("Error retrying message: {}", messageId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取消息的情绪分析结果
     */
    @GetMapping("/emotion/{messageId}")
    public ResponseEntity<Map<String, Object>> getMessageEmotion(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long messageId) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentication Token已过期，请重新生成"));
            }
            
            Optional<ChatHistory> message = chatService.getMessageById(messageId);
            
            if (message.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // 验证消息所有权
            if (!message.get().getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "权限不足"));
            }
            
            Map<String, Object> emotionData = new HashMap<>();
            emotionData.put("score", message.get().getEmotionScore());
            emotionData.put("label", message.get().getEmotionLabel());
            
            return ResponseEntity.ok(Map.of(
                    "messageId", messageId,
                    "emotion", emotionData
            ));
            
        } catch (Exception e) {
            log.error("Error getting message emotion: {}", messageId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 从token中提取用户ID
     */
    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            return null;
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid or expired JWT token");
            return null;
        }
        
        return jwtUtil.getUserIdFromToken(token);
    }
}