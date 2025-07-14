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

/**
 * 智能体API提交实体类
 * 用于管理用户提交的智能体API申请
 */
@Entity
@Table(name = "agent_submissions", indexes = {
    @Index(name = "idx_submission_submitter", columnList = "submitter_id"),
    @Index(name = "idx_submission_status", columnList = "status"),
    @Index(name = "idx_submission_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AgentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "智能体名称不能为空")
    @Size(min = 2, max = 50, message = "智能体名称长度必须在2-50个字符之间")
    @Column(nullable = false, length = 50)
    private String name;

    @NotBlank(message = "智能体描述不能为空")
    @Size(min = 10, max = 500, message = "描述长度必须在10-500个字符之间")
    @Column(nullable = false, length = 500)
    private String description;

    @NotBlank(message = "API地址不能为空")
    @Size(max = 200, message = "API地址长度不能超过200个字符")
    @Column(name = "api_url", nullable = false, length = 200)
    private String apiUrl;

    @NotBlank(message = "应用ID不能为空")
    @Size(max = 100, message = "应用ID长度不能超过100个字符")
    @Column(name = "app_id", nullable = false, length = 100)
    private String appId;

    @NotBlank(message = "API密钥不能为空")
    @Size(max = 500, message = "API密钥长度不能超过500个字符")
    @Column(name = "api_key", nullable = false, length = 500)
    private String apiKey;

    @NotBlank(message = "Token不能为空")
    @Size(max = 1000, message = "Token长度不能超过1000个字符")
    @Column(name = "token", nullable = false, length = 1000)
    private String token;

    @NotBlank(message = "智能体类别不能为空")
    @Size(max = 50, message = "类别长度不能超过50个字符")
    @Column(nullable = false, length = 50)
    private String category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submitter_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User submitter;

    @Enumerated(EnumType.STRING)
    @Column(name = "submitter_role", nullable = false)
    private User.UserRole submitterRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User reviewer;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Size(max = 500, message = "审核备注长度不能超过500个字符")
    @Column(name = "review_notes", length = 500)
    private String reviewNotes;

    @Column(name = "agent_id")
    private Long agentId; // 审核通过后创建的智能体ID

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 提交状态枚举
     */
    public enum SubmissionStatus {
        PENDING("待审核"),
        APPROVED("已通过"),
        REJECTED("已拒绝"),
        TESTING("测试中");

        private final String description;

        SubmissionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 便利方法
    public boolean isPending() {
        return SubmissionStatus.PENDING.equals(this.status);
    }

    public boolean isApproved() {
        return SubmissionStatus.APPROVED.equals(this.status);
    }

    public boolean isRejected() {
        return SubmissionStatus.REJECTED.equals(this.status);
    }

    public boolean isTesting() {
        return SubmissionStatus.TESTING.equals(this.status);
    }

    public void approve(User reviewer, String notes, Long agentId) {
        this.status = SubmissionStatus.APPROVED;
        this.reviewer = reviewer;
        this.reviewTime = LocalDateTime.now();
        this.reviewNotes = notes;
        this.agentId = agentId;
    }

    public void reject(User reviewer, String notes) {
        this.status = SubmissionStatus.REJECTED;
        this.reviewer = reviewer;
        this.reviewTime = LocalDateTime.now();
        this.reviewNotes = notes;
    }

    public void setTesting(User reviewer, String notes) {
        this.status = SubmissionStatus.TESTING;
        this.reviewer = reviewer;
        this.reviewTime = LocalDateTime.now();
        this.reviewNotes = notes;
    }
}