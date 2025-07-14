package com.lingxi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 成绩记录实体类
 * 存储学生成绩数据，支持成绩趋势预测和分析
 */
@Entity
@Table(name = "grade_records", indexes = {
    @Index(name = "idx_grade_user", columnList = "user_id"),
    @Index(name = "idx_grade_subject", columnList = "subject"),
    @Index(name = "idx_grade_exam_date", columnList = "exam_date"),
    @Index(name = "idx_grade_type", columnList = "grade_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GradeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "科目不能为空")
    @Size(max = 50, message = "科目名称长度不能超过50个字符")
    @Column(nullable = false, length = 50)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_type", nullable = false)
    private GradeType gradeType;

    @DecimalMin(value = "0.0", message = "成绩不能小于0")
    @DecimalMax(value = "100.0", message = "成绩不能大于100")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @DecimalMin(value = "0.0", message = "满分不能小于0")
    @Column(name = "full_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal fullScore = new BigDecimal("100.00");

    @Size(max = 100, message = "考试名称长度不能超过100个字符")
    @Column(name = "exam_name", length = 100)
    private String examName;

    @Column(name = "exam_date", nullable = false)
    private LocalDateTime examDate;

    @Size(max = 50, message = "学期信息长度不能超过50个字符")
    @Column(length = 50)
    private String semester;

    @Size(max = 50, message = "年级信息长度不能超过50个字符")
    @Column(length = 50)
    private String grade;

    @Size(max = 50, message = "班级信息长度不能超过50个字符")
    @Column(length = 50)
    private String className;

    // 排名信息
    @Column(name = "class_rank")
    private Integer classRank;

    @Column(name = "grade_rank")
    private Integer gradeRank;

    @Column(name = "total_students")
    private Integer totalStudents;

    // 统计信息
    @Column(name = "class_average", precision = 5, scale = 2)
    private BigDecimal classAverage;

    @Column(name = "grade_average", precision = 5, scale = 2)
    private BigDecimal gradeAverage;

    @Column(name = "highest_score", precision = 5, scale = 2)
    private BigDecimal highestScore;

    @Column(name = "lowest_score", precision = 5, scale = 2)
    private BigDecimal lowestScore;

    // 预测相关字段
    @Column(name = "predicted_next_score", precision = 5, scale = 2)
    private BigDecimal predictedNextScore;

    @Column(name = "prediction_confidence", precision = 3, scale = 2)
    private BigDecimal predictionConfidence;

    @Column(name = "trend_direction")
    @Enumerated(EnumType.STRING)
    private TrendDirection trendDirection;

    // 学习时长（分钟）
    @Column(name = "study_duration_minutes")
    private Integer studyDurationMinutes;

    // 错题数量
    @Column(name = "wrong_questions_count")
    private Integer wrongQuestionsCount;

    // 难题数量
    @Column(name = "difficult_questions_count")
    private Integer difficultQuestionsCount;

    // 扩展属性（JSON格式）
    @ElementCollection
    @CollectionTable(name = "grade_attributes", joinColumns = @JoinColumn(name = "grade_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value", length = 500)
    private Map<String, String> attributes;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 成绩类型枚举
    public enum GradeType {
        QUIZ("小测验"),
        HOMEWORK("作业"),
        MIDTERM("期中考试"),
        FINAL("期末考试"),
        MONTHLY("月考"),
        PRACTICE("练习"),
        COMPETITION("竞赛"),
        OTHER("其他");

        private final String description;

        GradeType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 趋势方向枚举
    public enum TrendDirection {
        IMPROVING("上升"),
        STABLE("稳定"),
        DECLINING("下降"),
        UNKNOWN("未知");

        private final String description;

        TrendDirection(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 便利方法
    public BigDecimal getPercentageScore() {
        if (fullScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return score.divide(fullScore, 4, BigDecimal.ROUND_HALF_UP)
                   .multiply(new BigDecimal("100"));
    }

    public boolean isPassingGrade() {
        return getPercentageScore().compareTo(new BigDecimal("60")) >= 0;
    }

    public boolean isExcellentGrade() {
        return getPercentageScore().compareTo(new BigDecimal("90")) >= 0;
    }

    public boolean isGoodGrade() {
        return getPercentageScore().compareTo(new BigDecimal("80")) >= 0;
    }

    public String getGradeLevel() {
        BigDecimal percentage = getPercentageScore();
        if (percentage.compareTo(new BigDecimal("90")) >= 0) {
            return "优秀";
        } else if (percentage.compareTo(new BigDecimal("80")) >= 0) {
            return "良好";
        } else if (percentage.compareTo(new BigDecimal("70")) >= 0) {
            return "中等";
        } else if (percentage.compareTo(new BigDecimal("60")) >= 0) {
            return "及格";
        } else {
            return "不及格";
        }
    }

    public boolean hasPrediction() {
        return predictedNextScore != null && predictionConfidence != null;
    }

    public boolean isImprovingTrend() {
        return TrendDirection.IMPROVING.equals(trendDirection);
    }

    public boolean isDecliningTrend() {
        return TrendDirection.DECLINING.equals(trendDirection);
    }

    public boolean isStableTrend() {
        return TrendDirection.STABLE.equals(trendDirection);
    }
}