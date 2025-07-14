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
 * 开发者申请实体类
 * 记录用户申请成为开发者的请求和审核状态
 */
@Entity
@Table(name = "developer_requests", indexes = {
    @Index(name = "idx_dev_req_user", columnList = "user_id"),
    @Index(name = "idx_dev_req_status", columnList = "status"),
    @Index(name = "idx_dev_req_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DeveloperRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User user;

    @NotBlank(message = "申请理由不能为空")
    @Size(min = 10, max = 1000, message = "申请理由长度必须在10-1000个字符之间")
    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Size(max = 500, message = "技能描述长度不能超过500个字符")
    @Column(name = "skills", length = 500)
    private String skills;

    @Size(max = 500, message = "项目经验长度不能超过500个字符")
    @Column(name = "experience", length = 500)
    private String experience;

    @Size(max = 200, message = "联系方式长度不能超过200个字符")
    @Column(name = "contact_info", length = 200)
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User reviewer;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Size(max = 500, message = "审核备注长度不能超过500个字符")
    @Column(name = "review_notes", length = 500)
    private String reviewNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 申请状态枚举
    public enum RequestStatus {
        PENDING("待审核"),
        APPROVED("已批准"),
        REJECTED("已拒绝"),
        CANCELLED("已取消");

        private final String description;

        RequestStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 便利方法
    public boolean isPending() {
        return RequestStatus.PENDING.equals(this.status);
    }

    public boolean isApproved() {
        return RequestStatus.APPROVED.equals(this.status);
    }

    public boolean isRejected() {
        return RequestStatus.REJECTED.equals(this.status);
    }

    public boolean isCancelled() {
        return RequestStatus.CANCELLED.equals(this.status);
    }

    public void approve(User reviewer, String notes) {
        this.status = RequestStatus.APPROVED;
        this.reviewer = reviewer;
        this.reviewTime = LocalDateTime.now();
        this.reviewNotes = notes;
    }

    public void reject(User reviewer, String notes) {
        this.status = RequestStatus.REJECTED;
        this.reviewer = reviewer;
        this.reviewTime = LocalDateTime.now();
        this.reviewNotes = notes;
    }

    public void cancel() {
        this.status = RequestStatus.CANCELLED;
    }
}