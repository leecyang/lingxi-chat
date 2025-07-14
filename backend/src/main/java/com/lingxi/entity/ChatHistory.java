package com.lingxi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 对话历史实体类
 * 记录用户与智能体的对话历史，支持个性化上下文
 */
@Entity
@Table(name = "chat_history", indexes = {
    @Index(name = "idx_chat_user_session", columnList = "user_id, session_id"),
    @Index(name = "idx_chat_created_at", columnList = "created_at"),
    @Index(name = "idx_chat_agent", columnList = "agent_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"chatHistories", "conversationLogs", "developerApplications", "agents"})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_id")
    @JsonIgnoreProperties({"chatHistories", "conversationLogs", "user", "developer"})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Agent agent;

    @NotBlank(message = "会话ID不能为空")
    @Size(max = 100, message = "会话ID长度不能超过100个字符")
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 5000, message = "消息内容长度不能超过5000个字符")
    @Column(nullable = false, length = 5000)
    private String content;

    @Size(max = 2000, message = "原始消息长度不能超过2000个字符")
    @Column(name = "raw_content", length = 2000)
    private String rawContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.SUCCESS;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Size(max = 500, message = "错误信息长度不能超过500个字符")
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    // 消息元数据（JSON格式）
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // 情感分析结果
    @Column(name = "emotion_score")
    private Double emotionScore;

    @Size(max = 50, message = "情感标签长度不能超过50个字符")
    @Column(name = "emotion_label", length = 50)
    private String emotionLabel;

    // 消息序号（在会话中的顺序）
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    // 父消息ID（用于回复链）
    @Column(name = "parent_message_id")
    private Long parentMessageId;

    // 是否被用户标记为有用
    @Column(name = "is_helpful")
    private Boolean isHelpful;

    // 用户评分（1-5星）
    @Column(name = "user_rating")
    private Integer userRating;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 消息类型枚举
    public enum MessageType {
        USER("用户消息"),
        AGENT("智能体回复"),
        SYSTEM("系统消息"),
        ERROR("错误消息");

        private final String description;

        MessageType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 消息状态枚举
    public enum MessageStatus {
        SUCCESS("成功"),
        PENDING("处理中"),
        FAILED("失败"),
        TIMEOUT("超时"),
        RATE_LIMITED("限流");

        private final String description;

        MessageStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 便利方法
    public boolean isUserMessage() {
        return MessageType.USER.equals(this.messageType);
    }

    public boolean isAgentMessage() {
        return MessageType.AGENT.equals(this.messageType);
    }

    public boolean isSystemMessage() {
        return MessageType.SYSTEM.equals(this.messageType);
    }

    public boolean isSuccessful() {
        return MessageStatus.SUCCESS.equals(this.status);
    }

    public boolean isFailed() {
        return MessageStatus.FAILED.equals(this.status) || 
               MessageStatus.TIMEOUT.equals(this.status);
    }

    public boolean isPending() {
        return MessageStatus.PENDING.equals(this.status);
    }

    // 获取用户ID的便利方法
    public Long getUserId() {
        return this.user != null ? this.user.getId() : null;
    }

    // 获取智能体ID的便利方法
    public Long getAgentId() {
        return this.agent != null ? this.agent.getId() : null;
    }

    // 构造器便利方法
    public static ChatHistory createUserMessage(User user, String sessionId, String content, Integer sequenceNumber) {
        ChatHistory history = new ChatHistory();
        history.setUser(user);
        history.setSessionId(sessionId);
        history.setMessageType(MessageType.USER);
        history.setContent(content);
        history.setRawContent(content);
        history.setSequenceNumber(sequenceNumber);
        history.setStatus(MessageStatus.SUCCESS);
        return history;
    }

    public static ChatHistory createAgentMessage(User user, Agent agent, String sessionId, 
                                               String content, Integer sequenceNumber, Long responseTime) {
        ChatHistory history = new ChatHistory();
        history.setUser(user);
        history.setAgent(agent);
        history.setSessionId(sessionId);
        history.setMessageType(MessageType.AGENT);
        history.setContent(content);
        history.setSequenceNumber(sequenceNumber);
        history.setResponseTimeMs(responseTime);
        history.setStatus(MessageStatus.SUCCESS);
        return history;
    }

    public static ChatHistory createErrorMessage(User user, String sessionId, String errorMessage, Integer sequenceNumber) {
        ChatHistory history = new ChatHistory();
        history.setUser(user);
        history.setSessionId(sessionId);
        history.setMessageType(MessageType.ERROR);
        history.setContent("系统错误");
        history.setErrorMessage(errorMessage);
        history.setSequenceNumber(sequenceNumber);
        history.setStatus(MessageStatus.FAILED);
        return history;
    }
}