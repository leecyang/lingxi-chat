package com.lingxi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 对话日志实体类
 * 用于管理员审核和监控用户与智能体的对话记录
 */
@Entity
@Table(name = "conversation_logs", indexes = {
    @Index(name = "idx_conv_log_user", columnList = "user_id"),
    @Index(name = "idx_conv_log_agent", columnList = "agent_id"),
    @Index(name = "idx_conv_log_session", columnList = "session_id"),
    @Index(name = "idx_conv_log_status", columnList = "status"),
    @Index(name = "idx_conv_log_created", columnList = "created_at"),
    @Index(name = "idx_conv_log_flagged", columnList = "is_flagged")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ConversationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Agent agent;

    @NotBlank(message = "会话ID不能为空")
    @Size(max = 100, message = "会话ID长度不能超过100个字符")
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "message_count", nullable = false)
    private Integer messageCount = 0;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "cost_amount")
    private Double costAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogStatus status = LogStatus.ACTIVE;

    @Column(name = "is_flagged", nullable = false)
    private Boolean isFlagged = false;

    @Size(max = 500, message = "标记原因长度不能超过500个字符")
    @Column(name = "flag_reason", length = 500)
    private String flagReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flagged_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User flaggedBy;

    @Column(name = "flagged_at")
    private LocalDateTime flaggedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_rating")
    private ContentRating contentRating;

    @Size(max = 1000, message = "摘要长度不能超过1000个字符")
    @Column(name = "summary", length = 1000)
    private String summary;

    // 存储额外的元数据（JSON格式）
    @ElementCollection
    @CollectionTable(name = "conversation_metadata", joinColumns = @JoinColumn(name = "conversation_log_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 1000)
    private Map<String, String> metadata;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 日志状态枚举
    public enum LogStatus {
        ACTIVE("活跃"),
        COMPLETED("已完成"),
        TERMINATED("已终止"),
        ARCHIVED("已归档"),
        DELETED("已删除");

        private final String description;

        LogStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 内容评级枚举
    public enum ContentRating {
        SAFE("安全"),
        QUESTIONABLE("可疑"),
        INAPPROPRIATE("不当"),
        HARMFUL("有害");

        private final String description;

        ContentRating(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 便利方法
    public boolean isActive() {
        return LogStatus.ACTIVE.equals(this.status);
    }

    public boolean isCompleted() {
        return LogStatus.COMPLETED.equals(this.status);
    }

    public boolean isTerminated() {
        return LogStatus.TERMINATED.equals(this.status);
    }

    public boolean isArchived() {
        return LogStatus.ARCHIVED.equals(this.status);
    }

    public boolean isDeleted() {
        return LogStatus.DELETED.equals(this.status);
    }

    public void flag(User flaggedBy, String reason) {
        this.isFlagged = true;
        this.flaggedBy = flaggedBy;
        this.flaggedAt = LocalDateTime.now();
        this.flagReason = reason;
    }

    public void unflag() {
        this.isFlagged = false;
        this.flaggedBy = null;
        this.flaggedAt = null;
        this.flagReason = null;
    }

    public void complete() {
        this.status = LogStatus.COMPLETED;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void terminate(String reason) {
        this.status = LogStatus.TERMINATED;
        this.lastActivityAt = LocalDateTime.now();
        if (reason != null && !reason.trim().isEmpty()) {
            this.flag(null, "会话被终止: " + reason);
        }
    }

    public void archive() {
        this.status = LogStatus.ARCHIVED;
    }

    public void incrementMessageCount() {
        this.messageCount++;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void addTokens(int tokens) {
        if (this.totalTokens == null) {
            this.totalTokens = tokens;
        } else {
            this.totalTokens += tokens;
        }
    }

    public void addCost(double cost) {
        if (this.costAmount == null) {
            this.costAmount = cost;
        } else {
            this.costAmount += cost;
        }
    }

    public void updateDuration() {
        if (this.createdAt != null && this.lastActivityAt != null) {
            this.durationMinutes = (int) java.time.Duration.between(this.createdAt, this.lastActivityAt).toMinutes();
        }
    }

    public boolean needsReview() {
        return this.isFlagged || 
               ContentRating.QUESTIONABLE.equals(this.contentRating) ||
               ContentRating.INAPPROPRIATE.equals(this.contentRating) ||
               ContentRating.HARMFUL.equals(this.contentRating);
    }
}