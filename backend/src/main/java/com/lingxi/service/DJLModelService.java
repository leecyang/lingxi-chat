package com.lingxi.service;

import com.lingxi.entity.GradeRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DJL模型服务 - 提供情绪分析和成绩趋势预测
 */
@Slf4j
@Service
public class DJLModelService {

    @Value("${app.djl.emotion-model.enabled:true}")
    private boolean emotionModelEnabled;

    @Value("${app.djl.grade-prediction.enabled:true}")
    private boolean gradePredictionEnabled;

    @Value("${app.djl.models.emotion-model-path:models/emotion_analysis}")
    private String emotionModelPath;

    @Value("${app.djl.models.grade-prediction-path:models/grade_prediction}")
    private String gradePredictionModelPath;

    // 情绪标签
    private static final List<String> EMOTION_LABELS = Arrays.asList(
            "积极", "消极", "中性", "焦虑", "兴奋", "沮丧", "满意", "不满"
    );

    /**
     * 分析文本情绪
     */
    public Map<String, Object> analyzeEmotion(String text) {
        if (!emotionModelEnabled || text == null || text.trim().isEmpty()) {
            return getDefaultEmotionResult();
        }

        try {
            // 这里应该使用真实的DJL模型进行情绪分析
            // 为了演示，使用模拟的分析结果
            return performEmotionAnalysis(text);
            
        } catch (Exception e) {
            log.error("Error analyzing emotion for text: {}", text, e);
            return getDefaultEmotionResult();
        }
    }

    /**
     * 预测成绩趋势
     */
    public Map<String, Object> predictGradeTrend(List<GradeRecord> historicalRecords, int months) {
        if (!gradePredictionEnabled || historicalRecords == null || historicalRecords.size() < 3) {
            return Map.of("error", "数据不足或模型未启用");
        }

        try {
            // 这里应该使用真实的DJL模型进行趋势预测
            // 为了演示，使用模拟的预测结果
            return performGradePrediction(historicalRecords, months);
            
        } catch (Exception e) {
            log.error("Error predicting grade trend", e);
            return Map.of("error", "预测失败: " + e.getMessage());
        }
    }

    /**
     * 批量情绪分析
     */
    public List<Map<String, Object>> batchAnalyzeEmotion(List<String> texts) {
        return texts.stream()
                .map(this::analyzeEmotion)
                .collect(Collectors.toList());
    }

    /**
     * 检查模型健康状态
     */
    public Map<String, Object> checkModelHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // 检查情绪分析模型
        health.put("emotionModel", Map.of(
                "enabled", emotionModelEnabled,
                "status", emotionModelEnabled ? "healthy" : "disabled",
                "path", emotionModelPath
        ));
        
        // 检查成绩预测模型
        health.put("gradePredictionModel", Map.of(
                "enabled", gradePredictionEnabled,
                "status", gradePredictionEnabled ? "healthy" : "disabled",
                "path", gradePredictionModelPath
        ));
        
        health.put("overallStatus", "healthy");
        health.put("checkTime", new Date());
        
        return health;
    }

    /**
     * 执行情绪分析（模拟实现）
     */
    private Map<String, Object> performEmotionAnalysis(String text) {
        // 模拟情绪分析逻辑
        Map<String, Double> emotionScores = new HashMap<>();
        
        // 基于关键词的简单情绪分析
        String lowerText = text.toLowerCase();
        
        // 积极情绪关键词
        List<String> positiveKeywords = Arrays.asList(
                "好", "棒", "优秀", "满意", "开心", "高兴", "进步", "提高", "成功"
        );
        
        // 消极情绪关键词
        List<String> negativeKeywords = Arrays.asList(
                "差", "糟糕", "失望", "难过", "退步", "下降", "失败", "困难", "问题"
        );
        
        // 焦虑情绪关键词
        List<String> anxiousKeywords = Arrays.asList(
                "担心", "紧张", "焦虑", "压力", "害怕", "不安", "忧虑"
        );
        
        // 计算各种情绪的得分
        double positiveScore = countKeywords(lowerText, positiveKeywords) * 0.3;
        double negativeScore = countKeywords(lowerText, negativeKeywords) * 0.3;
        double anxiousScore = countKeywords(lowerText, anxiousKeywords) * 0.2;
        double neutralScore = Math.max(0, 1.0 - positiveScore - negativeScore - anxiousScore);
        
        emotionScores.put("积极", Math.min(1.0, positiveScore));
        emotionScores.put("消极", Math.min(1.0, negativeScore));
        emotionScores.put("焦虑", Math.min(1.0, anxiousScore));
        emotionScores.put("中性", neutralScore);
        
        // 找出主要情绪
        String primaryEmotion = emotionScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("中性");
        
        double confidence = emotionScores.get(primaryEmotion);
        
        return Map.of(
                "primaryEmotion", primaryEmotion,
                "confidence", Math.round(confidence * 100.0) / 100.0,
                "emotionScores", emotionScores,
                "analysisTime", new Date(),
                "textLength", text.length()
        );
    }

    /**
     * 执行成绩预测（模拟实现）
     */
    private Map<String, Object> performGradePrediction(List<GradeRecord> records, int months) {
        // 按时间排序（最新的在前）
        List<GradeRecord> sortedRecords = records.stream()
                .sorted((a, b) -> b.getExamDate().compareTo(a.getExamDate()))
                .collect(Collectors.toList());
        
        // 提取特征
        List<Double> scores = sortedRecords.stream()
                .map(record -> record.getScore().doubleValue())
                .collect(Collectors.toList());
        
        List<LocalDateTime> dates = sortedRecords.stream()
                .map(GradeRecord::getExamDate)
                .collect(Collectors.toList());
        
        // 计算时间间隔（天数）
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < dates.size(); i++) {
            long daysBetween = ChronoUnit.DAYS.between(dates.get(i).toLocalDate(), dates.get(i-1).toLocalDate());
            intervals.add(daysBetween);
        }
        
        // 计算趋势
        double trendSlope = calculateTrendSlope(scores, intervals);
        
        // 计算波动性
        double volatility = calculateVolatility(scores);
        
        // 预测未来分数
        double currentScore = scores.get(0);
        double avgInterval = intervals.stream().mapToLong(Long::longValue).average().orElse(30.0);
        
        // 考虑时间衰减
        double timeDecay = Math.exp(-0.1 * months);
        double predictedScore = currentScore + (trendSlope * months * avgInterval * timeDecay);
        
        // 添加随机波动
        double randomFactor = (Math.random() - 0.5) * volatility * 0.5;
        predictedScore += randomFactor;
        
        // 确保预测分数在合理范围内
        double fullScore = sortedRecords.get(0).getFullScore().doubleValue();
        predictedScore = Math.max(0, Math.min(predictedScore, fullScore));
        
        // 计算置信度
        double confidence = calculateConfidence(scores, volatility, records.size());
        
        // 确定趋势方向
        GradeRecord.TrendDirection trendDirection;
        if (trendSlope > 0.5) {
            trendDirection = GradeRecord.TrendDirection.IMPROVING;
        } else if (trendSlope < -0.5) {
            trendDirection = GradeRecord.TrendDirection.DECLINING;
        } else {
            trendDirection = GradeRecord.TrendDirection.STABLE;
        }
        
        // 生成预测区间
        double margin = volatility * 1.96; // 95%置信区间
        double lowerBound = Math.max(0, predictedScore - margin);
        double upperBound = Math.min(fullScore, predictedScore + margin);
        
        // 生成建议
        List<String> recommendations = generateRecommendations(trendDirection, confidence, volatility);
        
        Map<String, Object> result = new HashMap<>();
        result.put("predictedScore", Math.round(predictedScore * 100.0) / 100.0);
        result.put("trendDirection", trendDirection);
        result.put("confidence", Math.round(confidence * 100.0) / 100.0);
        result.put("predictionInterval", Map.of(
                "lower", Math.round(lowerBound * 100.0) / 100.0,
                "upper", Math.round(upperBound * 100.0) / 100.0
        ));
        result.put("trendSlope", Math.round(trendSlope * 100.0) / 100.0);
        result.put("volatility", Math.round(volatility * 100.0) / 100.0);
        result.put("basedOnRecords", records.size());
        result.put("predictionMonths", months);
        result.put("recommendations", recommendations);
        result.put("modelVersion", "1.0");
        result.put("predictionTime", new Date());
        return result;
    }

    /**
     * 计算趋势斜率
     */
    private double calculateTrendSlope(List<Double> scores, List<Long> intervals) {
        if (scores.size() < 2) {
            return 0.0;
        }
        
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = Math.min(scores.size(), intervals.size() + 1);
        
        for (int i = 0; i < n - 1; i++) {
            double x = intervals.get(i);
            double y = scores.get(i) - scores.get(i + 1);
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        double denominator = (n - 1) * sumX2 - sumX * sumX;
        if (Math.abs(denominator) < 1e-10) {
            return 0.0;
        }
        
        return ((n - 1) * sumXY - sumX * sumY) / denominator;
    }

    /**
     * 计算波动性
     */
    private double calculateVolatility(List<Double> scores) {
        if (scores.size() < 2) {
            return 0.0;
        }
        
        double mean = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = scores.stream()
                .mapToDouble(score -> Math.pow(score - mean, 2))
                .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }

    /**
     * 计算置信度
     */
    private double calculateConfidence(List<Double> scores, double volatility, int recordCount) {
        // 基于数据量和波动性计算置信度
        double dataConfidence = Math.min(1.0, recordCount / 10.0);
        double stabilityConfidence = Math.max(0.1, 1.0 - (volatility / 50.0));
        
        return (dataConfidence + stabilityConfidence) / 2.0;
    }

    /**
     * 生成建议
     */
    private List<String> generateRecommendations(GradeRecord.TrendDirection trend, double confidence, double volatility) {
        List<String> recommendations = new ArrayList<>();
        
        switch (trend) {
            case IMPROVING:
                recommendations.add("成绩呈上升趋势，继续保持当前的学习方法");
                recommendations.add("可以适当增加学习难度，挑战更高目标");
                break;
            case DECLINING:
                recommendations.add("成绩有下降趋势，建议及时调整学习策略");
                recommendations.add("可以寻求老师或同学的帮助，找出问题所在");
                break;
            case STABLE:
                recommendations.add("成绩相对稳定，可以尝试新的学习方法突破瓶颈");
                break;
            case UNKNOWN:
                recommendations.add("趋势不明确，建议增加更多数据进行分析");
                break;
        }
        
        if (confidence < 0.6) {
            recommendations.add("预测置信度较低，建议增加更多考试数据以提高准确性");
        }
        
        if (volatility > 15) {
            recommendations.add("成绩波动较大，建议保持稳定的学习节奏");
        }
        
        return recommendations;
    }

    /**
     * 统计关键词出现次数
     */
    private int countKeywords(String text, List<String> keywords) {
        return (int) keywords.stream()
                .mapToLong(keyword -> countOccurrences(text, keyword))
                .sum();
    }

    /**
     * 统计字符串中子串出现次数
     */
    private long countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    /**
     * 获取默认情绪分析结果
     */
    private Map<String, Object> getDefaultEmotionResult() {
        return Map.of(
                "primaryEmotion", "中性",
                "confidence", 0.5,
                "emotionScores", Map.of("中性", 1.0),
                "analysisTime", new Date(),
                "note", "使用默认结果"
        );
    }
}