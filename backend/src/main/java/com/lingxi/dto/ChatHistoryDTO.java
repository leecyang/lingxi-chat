package com.lingxi.dto;

import com.lingxi.entity.ChatHistory;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 聊天历史DTO类
 * 用于API响应，避免懒加载序列化问题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatHistoryDTO {
    
    private Long id;
    private Long userId;
    private String username;
    private Long agentId;
    private String agentName;
    private String sessionId;
    private String messageType;
    private String content;
    private String rawContent;
    private String status;
    private Long responseTimeMs;
    private String errorMessage;
    private String metadata;
    private Double emotionScore;
    private String emotionLabel;
    private Integer sequenceNumber;
    private Long parentMessageId;
    private Boolean isHelpful;
    private Integer userRating;
    private LocalDateTime createdAt;
    
    /**
     * 从ChatHistory实体转换为DTO
     */
    public static ChatHistoryDTO fromEntity(ChatHistory entity) {
        if (entity == null) {
            return null;
        }
        
        return ChatHistoryDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .username(entity.getUser() != null ? entity.getUser().getUsername() : null)
                .agentId(entity.getAgent() != null ? entity.getAgent().getId() : null)
                .agentName(entity.getAgent() != null ? entity.getAgent().getName() : null)
                .sessionId(entity.getSessionId())
                .messageType(entity.getMessageType() != null ? entity.getMessageType().name() : null)
                .content(entity.getContent())
                .rawContent(entity.getRawContent())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .responseTimeMs(entity.getResponseTimeMs())
                .errorMessage(entity.getErrorMessage())
                .metadata(entity.getMetadata())
                .emotionScore(entity.getEmotionScore())
                .emotionLabel(entity.getEmotionLabel())
                .sequenceNumber(entity.getSequenceNumber())
                .parentMessageId(entity.getParentMessageId())
                .isHelpful(entity.getIsHelpful())
                .userRating(entity.getUserRating())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    
    // 便利方法
    public boolean isUserMessage() {
        return "USER".equals(this.messageType);
    }
    
    public boolean isAgentMessage() {
        return "AGENT".equals(this.messageType);
    }
    
    public boolean isSystemMessage() {
        return "SYSTEM".equals(this.messageType);
    }
    
    public boolean isSuccessful() {
        return "SUCCESS".equals(this.status);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(this.status) || "TIMEOUT".equals(this.status);
    }
    
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }
}