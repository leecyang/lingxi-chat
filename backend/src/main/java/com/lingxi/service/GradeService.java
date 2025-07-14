package com.lingxi.service;

import com.lingxi.entity.GradeRecord;
import com.lingxi.repository.GradeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.lingxi.entity.User;

/**
 * 成绩管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRecordRepository gradeRecordRepository;
    private final DJLModelService djlModelService;

    /**
     * 添加成绩记录
     */
    @Transactional
    public GradeRecord addGradeRecord(GradeRecord gradeRecord, Long userId) {
        User user = new User();
        user.setId(userId);
        gradeRecord.setUser(user);
        gradeRecord.setCreatedAt(LocalDateTime.now());
        gradeRecord.setUpdatedAt(LocalDateTime.now());
        
        log.info("Adding grade record for user: {} subject: {}", userId, gradeRecord.getSubject());
        return gradeRecordRepository.save(gradeRecord);
    }

    /**
     * 批量添加成绩记录
     */
    @Transactional
    public List<GradeRecord> addGradeRecords(List<GradeRecord> gradeRecords, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId(userId);
        gradeRecords.forEach(record -> {
            record.setUser(user);
            record.setCreatedAt(now);
            record.setUpdatedAt(now);
        });
        
        log.info("Adding {} grade records for user: {}", gradeRecords.size(), userId);
        return gradeRecordRepository.saveAll(gradeRecords);
    }

    /**
     * 更新成绩记录
     */
    @Transactional
    public GradeRecord updateGradeRecord(Long recordId, GradeRecord gradeRecord, Long userId) {
        GradeRecord existingRecord = gradeRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在"));
        
        if (!existingRecord.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限修改此成绩记录");
        }
        
        // 更新字段
        existingRecord.setSubject(gradeRecord.getSubject());
        existingRecord.setExamName(gradeRecord.getExamName());
        existingRecord.setScore(gradeRecord.getScore());
        existingRecord.setFullScore(gradeRecord.getFullScore());
        existingRecord.setExamDate(gradeRecord.getExamDate());
        existingRecord.setGradeType(gradeRecord.getGradeType());
        existingRecord.setNotes(gradeRecord.getNotes());
        existingRecord.setUpdatedAt(LocalDateTime.now());
        
        log.info("Updating grade record: {} for user: {}", recordId, userId);
        return gradeRecordRepository.save(existingRecord);
    }

    /**
     * 删除成绩记录
     */
    @Transactional
    public void deleteGradeRecord(Long recordId, Long userId) {
        GradeRecord record = gradeRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在"));
        
        if (!record.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限删除此成绩记录");
        }
        
        log.info("Deleting grade record: {} for user: {}", recordId, userId);
        gradeRecordRepository.delete(record);
    }

    /**
     * 获取成绩记录详情
     */
    public Optional<GradeRecord> getGradeRecord(Long recordId, Long userId) {
        Optional<GradeRecord> record = gradeRecordRepository.findById(recordId);
        
        if (record.isPresent() && !record.get().getUser().getId().equals(userId)) {
            return Optional.empty();
        }
        
        return record;
    }

    /**
     * 获取用户的所有成绩记录
     */
    public Page<GradeRecord> getUserGradeRecords(Long userId, Pageable pageable) {
        return gradeRecordRepository.findByUser_IdOrderByExamDateDesc(userId, pageable);
    }

    /**
     * 根据科目获取成绩记录
     */
    public Page<GradeRecord> getGradeRecordsBySubject(Long userId, String subject, Pageable pageable) {
        return gradeRecordRepository.findByUser_IdAndSubjectOrderByExamDateDesc(userId, subject, pageable);
    }

    /**
     * 根据成绩类型获取成绩记录
     */
    public Page<GradeRecord> getGradeRecordsByType(Long userId, GradeRecord.GradeType gradeType, Pageable pageable) {
        return gradeRecordRepository.findByUser_IdAndGradeTypeOrderByExamDateDesc(userId, gradeType, pageable);
    }

    /**
     * 根据日期范围获取成绩记录
     */
    public Page<GradeRecord> getGradeRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return gradeRecordRepository.findByUser_IdAndExamDateBetweenOrderByExamDateDesc(userId, startDate, endDate, pageable);
    }

    /**
     * 获取科目成绩统计
     */
    public Map<String, Object> getSubjectStatistics(Long userId, String subject) {
        List<GradeRecord> records = gradeRecordRepository.findByUser_IdAndSubjectOrderByExamDateDesc(userId, subject);
        
        if (records.isEmpty()) {
            return Map.of("error", "暂无该科目成绩数据");
        }
        
        // 计算统计数据
        double avgScore = records.stream().mapToDouble(r -> r.getScore().doubleValue()).average().orElse(0.0);
        double maxScore = records.stream().mapToDouble(r -> r.getScore().doubleValue()).max().orElse(0.0);
        double minScore = records.stream().mapToDouble(r -> r.getScore().doubleValue()).min().orElse(0.0);
        
        // 计算百分比分数
        double avgPercentage = records.stream()
                .mapToDouble(r -> r.getScore().divide(r.getFullScore(), 4, RoundingMode.HALF_UP).doubleValue() * 100)
                .average().orElse(0.0);
        
        // 统计等级分布
        Map<String, Long> gradeDistribution = records.stream()
                .collect(Collectors.groupingBy(
                        GradeRecord::getGradeLevel,
                        Collectors.counting()
                ));
        
        return Map.of(
                "subject", subject,
                "totalRecords", records.size(),
                "averageScore", BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP),
                "maxScore", maxScore,
                "minScore", minScore,
                "averagePercentage", BigDecimal.valueOf(avgPercentage).setScale(2, RoundingMode.HALF_UP),
                "gradeDistribution", gradeDistribution,
                "latestRecord", records.get(0)
        );
    }

    /**
     * 获取整体成绩统计
     */
    public Map<String, Object> getOverallStatistics(Long userId) {
        List<GradeRecord> allRecords = gradeRecordRepository.findByUser_IdOrderByExamDateDesc(userId);
        
        if (allRecords.isEmpty()) {
            return Map.of("error", "暂无成绩数据");
        }
        
        // 按科目分组统计
        Map<String, List<GradeRecord>> subjectGroups = allRecords.stream()
                .collect(Collectors.groupingBy(GradeRecord::getSubject));
        
        Map<String, Object> subjectStats = new HashMap<>();
        for (Map.Entry<String, List<GradeRecord>> entry : subjectGroups.entrySet()) {
            List<GradeRecord> records = entry.getValue();
            double avgScore = records.stream().mapToDouble(r -> r.getScore().doubleValue()).average().orElse(0.0);
            subjectStats.put(entry.getKey(), Map.of(
                    "count", records.size(),
                    "average", BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP)
            ));
        }
        
        // 整体统计
        double overallAvg = allRecords.stream().mapToDouble(r -> r.getScore().doubleValue()).average().orElse(0.0);
        
        return Map.of(
                "totalRecords", allRecords.size(),
                "totalSubjects", subjectGroups.size(),
                "overallAverage", BigDecimal.valueOf(overallAvg).setScale(2, RoundingMode.HALF_UP),
                "subjectStatistics", subjectStats,
                "recentRecords", allRecords.stream().limit(5).collect(Collectors.toList())
        );
    }

    /**
     * 获取成绩趋势分析
     */
    public Map<String, Object> getGradeTrend(Long userId, String subject, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);
        
        List<GradeRecord> records = gradeRecordRepository
                .findByUser_IdAndSubjectAndExamDateBetweenOrderByExamDateAsc(userId, subject, startDate, endDate);
        
        if (records.isEmpty()) {
            return Map.of("error", "指定时间范围内暂无成绩数据");
        }
        
        // 计算趋势
        List<Map<String, Object>> trendData = records.stream()
                .map(record -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("date", record.getExamDate());
                    data.put("score", record.getScore());
                    data.put("percentage", record.getScore().divide(record.getFullScore(), 4, RoundingMode.HALF_UP).doubleValue() * 100);
                    data.put("examName", record.getExamName());
                    return data;
                })
                .collect(Collectors.toList());
        
        // 计算趋势方向
        String trendDirection = "stable";
        if (records.size() >= 2) {
            double firstScore = records.get(0).getScore().divide(records.get(0).getFullScore(), 4, RoundingMode.HALF_UP).doubleValue() * 100;
            double lastScore = records.get(records.size() - 1).getScore().divide(records.get(records.size() - 1).getFullScore(), 4, RoundingMode.HALF_UP).doubleValue() * 100;
            double diff = lastScore - firstScore;
            
            if (diff > 5) {
                trendDirection = "improving";
            } else if (diff < -5) {
                trendDirection = "declining";
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("subject", subject);
        result.put("period", months + "个月");
        result.put("trendData", trendData);
        result.put("trendDirection", trendDirection);
        result.put("recordCount", records.size());
        return result;
    }

    /**
     * 预测成绩趋势
     */
    public Map<String, Object> predictGradeTrend(Long userId, String subject, int months) {
        List<GradeRecord> historicalRecords = gradeRecordRepository
                .findByUser_IdAndSubjectOrderByExamDateDesc(userId, subject);
        
        if (historicalRecords.size() < 3) {
            return Map.of("error", "历史数据不足，无法进行预测（至少需要3条记录）");
        }
        
        // 使用DJL模型服务进行预测
        Map<String, Object> prediction = djlModelService.predictGradeTrend(historicalRecords, months);
        
        return Map.of(
                "subject", subject,
                "predictionPeriod", months + "个月",
                "historicalDataCount", historicalRecords.size(),
                "prediction", prediction
        );
    }

    /**
     * 获取排名信息
     */
    public Map<String, Object> getRanking(Long userId, String subject, String examName) {
        // 这里简化实现，实际应该根据具体业务需求实现排名逻辑
        List<GradeRecord> userRecords;
        
        if (examName != null && !examName.isEmpty()) {
            userRecords = gradeRecordRepository
                    .findByUser_IdAndSubjectAndExamNameOrderByExamDateDesc(userId, subject, examName);
        } else {
            userRecords = gradeRecordRepository
                    .findByUser_IdAndSubjectOrderByExamDateDesc(userId, subject);
        }
        
        if (userRecords.isEmpty()) {
            return Map.of("error", "暂无相关成绩数据");
        }
        
        GradeRecord latestRecord = userRecords.get(0);
        double userPercentage = latestRecord.getScore().divide(latestRecord.getFullScore(), 4, RoundingMode.HALF_UP).doubleValue() * 100;
        
        // 模拟排名数据（实际应该查询所有用户的成绩进行排名）
        return Map.of(
                "subject", subject,
                "examName", examName != null ? examName : "最近考试",
                "userScore", latestRecord.getScore(),
                "userPercentage", BigDecimal.valueOf(userPercentage).setScale(2, RoundingMode.HALF_UP),
                "gradeLevel", latestRecord.getGradeLevel(),
                "estimatedRank", "前30%", // 模拟数据
                "totalParticipants", 100 // 模拟数据
        );
    }

    /**
     * 获取用户的科目列表
     */
    public List<String> getUserSubjects(Long userId) {
        return gradeRecordRepository.findDistinctSubjectsByUser_Id(userId);
    }

    /**
     * 获取用户的考试列表
     */
    public List<String> getUserExams(Long userId, String subject) {
        if (subject != null && !subject.isEmpty()) {
            return gradeRecordRepository.findDistinctExamNamesByUser_IdAndSubject(userId, subject);
        } else {
            return gradeRecordRepository.findDistinctExamNamesByUser_Id(userId);
        }
    }

    /**
     * 导出成绩数据
     */
    public List<GradeRecord> exportGradeData(Long userId, String subject, LocalDate startDate, LocalDate endDate) {
        if (subject != null && !subject.isEmpty()) {
            if (startDate != null && endDate != null) {
                return gradeRecordRepository
                        .findByUser_IdAndSubjectAndExamDateBetweenOrderByExamDateDesc(userId, subject, startDate, endDate);
            } else {
                return gradeRecordRepository.findByUser_IdAndSubjectOrderByExamDateDesc(userId, subject);
            }
        } else {
            if (startDate != null && endDate != null) {
                return gradeRecordRepository
                        .findByUser_IdAndExamDateBetweenOrderByExamDateDesc(userId, startDate, endDate);
            } else {
                return gradeRecordRepository.findByUser_IdOrderByExamDateDesc(userId);
            }
        }
    }

    /**
     * 获取成绩分析报告
     */
    public Map<String, Object> getGradeAnalysis(Long userId, String subject, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);
        
        List<GradeRecord> records;
        if (subject != null && !subject.isEmpty()) {
            records = gradeRecordRepository
                    .findByUser_IdAndSubjectAndExamDateBetweenOrderByExamDateDesc(userId, subject, startDate, endDate);
        } else {
            records = gradeRecordRepository
                    .findByUser_IdAndExamDateBetweenOrderByExamDateDesc(userId, startDate, endDate);
        }
        
        if (records.isEmpty()) {
            return Map.of("error", "指定时间范围内暂无成绩数据");
        }
        
        // 分析数据
        double avgScore = records.stream().mapToDouble(r -> r.getScore().doubleValue()).average().orElse(0.0);
        double avgPercentage = records.stream()
                .mapToDouble(r -> r.getScore().divide(r.getFullScore(), 4, RoundingMode.HALF_UP).doubleValue() * 100)
                .average().orElse(0.0);
        
        // 进步分析
        String progressAnalysis = "稳定";
        if (records.size() >= 2) {
            double recentAvg = records.stream().limit(records.size() / 2)
                    .mapToDouble(r -> r.getScore().divide(r.getFullScore(), 4, RoundingMode.HALF_UP).doubleValue() * 100)
                    .average().orElse(0.0);
            double earlierAvg = records.stream().skip(records.size() / 2)
                    .mapToDouble(r -> r.getScore().divide(r.getFullScore(), 4, RoundingMode.HALF_UP).doubleValue() * 100)
                    .average().orElse(0.0);
            
            if (recentAvg > earlierAvg + 5) {
                progressAnalysis = "明显进步";
            } else if (recentAvg < earlierAvg - 5) {
                progressAnalysis = "需要加强";
            }
        }
        
        // 优势科目分析（如果是全科目分析）
        Map<String, Double> subjectAverage = new HashMap<>();
        if (subject == null || subject.isEmpty()) {
            subjectAverage = records.stream()
                    .collect(Collectors.groupingBy(
                            GradeRecord::getSubject,
                            Collectors.averagingDouble(r -> r.getScore().divide(r.getFullScore(), 4, RoundingMode.HALF_UP).doubleValue() * 100)
                    ));
        }
        
        return Map.of(
                "period", months + "个月",
                "totalRecords", records.size(),
                "averageScore", BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP),
                "averagePercentage", BigDecimal.valueOf(avgPercentage).setScale(2, RoundingMode.HALF_UP),
                "progressAnalysis", progressAnalysis,
                "subjectAnalysis", subjectAverage,
                "recommendations", generateRecommendations(avgPercentage, progressAnalysis)
        );
    }

    /**
     * 清空成绩记录
     */
    @Transactional
    public void clearGradeRecords(Long userId, String subject) {
        if (subject != null && !subject.isEmpty()) {
            gradeRecordRepository.deleteByUser_IdAndSubject(userId, subject);
            log.info("Cleared grade records for user: {} subject: {}", userId, subject);
        } else {
            gradeRecordRepository.deleteByUser_Id(userId);
            log.info("Cleared all grade records for user: {}", userId);
        }
    }

    /**
     * 生成学习建议
     */
    private List<String> generateRecommendations(double avgPercentage, String progressAnalysis) {
        List<String> recommendations = new ArrayList<>();
        
        if (avgPercentage >= 90) {
            recommendations.add("成绩优秀，继续保持！");
            recommendations.add("可以尝试更有挑战性的题目");
        } else if (avgPercentage >= 80) {
            recommendations.add("成绩良好，争取更进一步");
            recommendations.add("注意查漏补缺，巩固薄弱环节");
        } else if (avgPercentage >= 70) {
            recommendations.add("成绩中等，还有提升空间");
            recommendations.add("建议加强基础知识的学习");
        } else if (avgPercentage >= 60) {
            recommendations.add("成绩及格，需要努力提高");
            recommendations.add("建议制定详细的学习计划");
        } else {
            recommendations.add("成绩需要大幅提升");
            recommendations.add("建议寻求老师或同学的帮助");
        }
        
        if ("明显进步".equals(progressAnalysis)) {
            recommendations.add("最近进步明显，继续努力！");
        } else if ("需要加强".equals(progressAnalysis)) {
            recommendations.add("最近成绩有所下滑，需要调整学习方法");
        }
        
        return recommendations;
    }
}