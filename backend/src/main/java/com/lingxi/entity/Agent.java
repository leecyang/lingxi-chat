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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 智能体实体类
 * 管理智能体配置、审核状态和调用信息
 */
@Entity
@Table(name = "agents", indexes = {
    @Index(name = "idx_agent_name", columnList = "name"),
    @Index(name = "idx_agent_status", columnList = "status"),
    @Index(name = "idx_agent_creator", columnList = "creator_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "智能体名称不能为空")
    @Size(min = 2, max = 50, message = "智能体名称长度必须在2-50个字符之间")
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @NotBlank(message = "智能体显示名称不能为空")
    @Size(max = 100, message = "显示名称长度不能超过100个字符")
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    @Column(length = 500)
    private String description;

    @NotBlank(message = "模型ID不能为空")
    @Size(max = 100, message = "模型ID长度不能超过100个字符")
    @Column(name = "model_id", nullable = false, length = 100)
    private String modelId;

    @NotBlank(message = "API端点不能为空")
    @Size(max = 200, message = "API端点长度不能超过200个字符")
    @Column(nullable = false, length = 200)
    private String endpoint;

    @Size(max = 200, message = "API端点长度不能超过200个字符")
    @Column(length = 200)
    private String avatar;

    // 九天平台专用字段
    @Size(max = 100, message = "应用ID长度不能超过100个字符")
    @Column(name = "app_id", length = 100)
    private String appId;

    @Size(max = 500, message = "API密钥长度不能超过500个字符")
    @Column(name = "api_key", length = 500)
    private String apiKey;

    @Size(max = 1000, message = "Token长度不能超过1000个字符")
    @Column(name = "token", length = 1000)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentStatus status = AgentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentType type = AgentType.JIUTIAN;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User creator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approver_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User approver;

    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    @Size(max = 500, message = "审核备注长度不能超过500个字符")
    @Column(name = "approval_notes", length = 500)
    private String approvalNotes;

    // 智能体配置参数（JSON格式存储）
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "agent_config", joinColumns = @JoinColumn(name = "agent_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value", length = 1000)
    private Map<String, String> config;

    // 调用统计
    @Column(name = "total_calls", nullable = false)
    private Long totalCalls = 0L;

    @Column(name = "success_calls", nullable = false)
    private Long successCalls = 0L;

    @Column(name = "last_call_time")
    private LocalDateTime lastCallTime;

    @Column(name = "average_response_time")
    private Double averageResponseTime;

    // 是否启用
    @Column(nullable = false)
    private Boolean enabled = true;

    // 优先级（用于排序）
    @Column(nullable = false)
    private Integer priority = 0;

    // 智能体分类
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private AgentCategory category;

    // 标签（用于分类）
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "agent_tags", joinColumns = @JoinColumn(name = "agent_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 智能体状态枚举
    public enum AgentStatus {
        PENDING("待审核"),
        APPROVED("已审核"),
        REJECTED("已拒绝"),
        SUSPENDED("已暂停");

        private final String description;

        AgentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 智能体类型枚举
    public enum AgentType {
        JIUTIAN("九天大模型"),
        LOCAL("本地模型"),
        CUSTOM("自定义模型");

        private final String description;

        AgentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 智能体分类枚举
    public enum AgentCategory {
        MORAL_EDUCATION("德育引导", "点亮成长中的品格星光"),
        INTELLECTUAL_EDUCATION("智育助力", "你的专属学习规划伙伴/定制你的学霸进阶路"),
        PHYSICAL_EDUCATION("体育赋能", "活力计划与暖心提醒/运动打卡，活力满格！"),
        AESTHETIC_EDUCATION("美育熏陶", "创意路上的温暖同行者/画出你的专属艺术范儿"),
        LABOR_EDUCATION("劳育实践", "劳务协作，轻松搞定！"),
        LIFE_SUPPORT("生活护航", "全天候贴心守护/你的24小时智能陪伴"),
        COORDINATION_AGENT("统筹智能体", "灵犀智学团队通过九天大模型平台设计的专门为解决学生生活的全方位学习助手");

        private final String name;
        private final String description;

        AgentCategory(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    // 便利方法
    public boolean isApproved() {
        return AgentStatus.APPROVED.equals(this.status);
    }

    public boolean isPending() {
        return AgentStatus.PENDING.equals(this.status);
    }

    public boolean isRejected() {
        return AgentStatus.REJECTED.equals(this.status);
    }

    public boolean isSuspended() {
        return AgentStatus.SUSPENDED.equals(this.status);
    }

    public boolean isAvailable() {
        return isApproved() && enabled;
    }

    public void incrementCalls() {
        this.totalCalls++;
        this.lastCallTime = LocalDateTime.now();
    }

    public void incrementSuccessCalls() {
        this.successCalls++;
    }

    public void updateResponseTime(long responseTimeMs) {
        if (this.averageResponseTime == null) {
            this.averageResponseTime = (double) responseTimeMs;
        } else {
            // 简单的移动平均
            this.averageResponseTime = (this.averageResponseTime * 0.9) + (responseTimeMs * 0.1);
        }
    }

    public double getSuccessRate() {
        if (totalCalls == 0) {
            return 0.0;
        }
        return (double) successCalls / totalCalls * 100;
    }

    // 从配置中获取系统提示
    public String getSystemPrompt() {
        if (config != null) {
            return config.get("systemPrompt");
        }
        return null;
    }

    // 从配置中获取最大令牌数
    public Integer getMaxTokens() {
        if (config != null && config.get("maxTokens") != null) {
            try {
                return Integer.parseInt(config.get("maxTokens"));
            } catch (NumberFormatException e) {
                return 2048; // 默认值
            }
        }
        return 2048;
    }

    // 从配置中获取温度参数
    public Double getTemperature() {
        if (config != null && config.get("temperature") != null) {
            try {
                return Double.parseDouble(config.get("temperature"));
            } catch (NumberFormatException e) {
                return 0.7; // 默认值
            }
        }
        return 0.7;
    }

    // 从配置中获取top_p参数
    public Double getTopP() {
        if (config != null && config.get("topP") != null) {
            try {
                return Double.parseDouble(config.get("topP"));
            } catch (NumberFormatException e) {
                return 0.9; // 默认值
            }
        }
        return 0.9;
    }

    // 从配置中获取API token（重命名以避免与字段getter冲突）
    public String getConfigToken() {
        if (config != null) {
            return config.get("token");
        }
        return null;
    }
    
    // 获取九天平台token（优先使用直接字段，其次使用配置）
    public String getJiutianToken() {
        // 优先使用直接字段
        if (token != null && !token.trim().isEmpty()) {
            return token;
        }
        // 备用：从配置中获取
        return getConfigToken();
    }
}