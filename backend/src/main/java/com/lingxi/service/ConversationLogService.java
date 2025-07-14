package com.lingxi.service;

import com.lingxi.entity.ConversationLog;
import com.lingxi.entity.User;
import com.lingxi.entity.Agent;
import com.lingxi.entity.ChatHistory;
import com.lingxi.repository.ConversationLogRepository;
import com.lingxi.repository.UserRepository;
import com.lingxi.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * 对话日志服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationLogService {

    private final ConversationLogRepository conversationLogRepository;
    private final UserRepository userRepository;
    private final AgentRepository agentRepository;

    /**
     * 创建对话日志
     */
    @Transactional
    public ConversationLog createConversationLog(String sessionId, Long userId, Long agentId) {
        log.info("Creating conversation log for session: {}, user: {}, agent: {}", sessionId, userId, agentId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("智能体不存在"));
        
        // 检查是否已存在相同会话ID的日志
        Optional<ConversationLog> existingLog = conversationLogRepository.findBySessionId(sessionId);
        if (existingLog.isPresent()) {
            return existingLog.get();
        }
        
        ConversationLog conversationLog = new ConversationLog();
        conversationLog.setSessionId(sessionId);
        conversationLog.setUser(user);
        conversationLog.setAgent(agent);
        conversationLog.setStatus(ConversationLog.LogStatus.ACTIVE);
        conversationLog.setLastActivityAt(LocalDateTime.now());
        
        ConversationLog savedLog = conversationLogRepository.save(conversationLog);
        log.info("Conversation log created with ID: {}", savedLog.getId());
        
        return savedLog;
    }

    /**
     * 更新对话日志（添加消息时调用）
     */
    @Transactional
    public ConversationLog updateConversationLog(String sessionId, ChatHistory chatHistory) {
        ConversationLog conversationLog = conversationLogRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("对话日志不存在"));
        
        conversationLog.incrementMessageCount();
        
        // 估算token数量（简单估算：中文字符数 * 1.5，英文单词数 * 1.3）
        if (chatHistory.getContent() != null) {
            int estimatedTokens = estimateTokens(chatHistory.getContent());
            conversationLog.addTokens(estimatedTokens);
            
            // 估算成本（假设每1000个token成本0.01元）
            double estimatedCost = estimatedTokens * 0.00001;
            conversationLog.addCost(estimatedCost);
        }
        
        conversationLog.updateDuration();
        
        return conversationLogRepository.save(conversationLog);
    }

    /**
     * 完成对话日志
     */
    @Transactional
    public ConversationLog completeConversationLog(String sessionId, String summary) {
        log.info("Completing conversation log for session: {}", sessionId);
        
        ConversationLog conversationLog = conversationLogRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("对话日志不存在"));
        
        conversationLog.complete();
        if (summary != null && !summary.trim().isEmpty()) {
            conversationLog.setSummary(summary);
        }
        conversationLog.updateDuration();
        
        ConversationLog savedLog = conversationLogRepository.save(conversationLog);
        log.info("Conversation log {} completed", conversationLog.getId());
        
        return savedLog;
    }

    /**
     * 标记对话日志
     */
    @Transactional
    public ConversationLog flagConversationLog(Long logId, String reason, Long flaggedById) {
        log.info("Flagging conversation log: {} by user: {}", logId, flaggedById);
        
        ConversationLog conversationLog = conversationLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("对话日志不存在"));
        
        User flaggedBy = userRepository.findById(flaggedById)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 权限检查：只有管理员和开发者可以标记
        if (!flaggedBy.getRole().equals(User.UserRole.ADMIN) && 
            !flaggedBy.getRole().equals(User.UserRole.DEVELOPER)) {
            throw new RuntimeException("只有管理员和开发者可以标记对话日志");
        }
        
        conversationLog.flag(flaggedBy, reason);
        
        ConversationLog savedLog = conversationLogRepository.save(conversationLog);
        log.info("Conversation log {} flagged", logId);
        
        return savedLog;
    }

    /**
     * 取消标记对话日志
     */
    @Transactional
    public ConversationLog unflagConversationLog(Long logId, Long userId) {
        log.info("Unflagging conversation log: {} by user: {}", logId, userId);
        
        ConversationLog conversationLog = conversationLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("对话日志不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 权限检查：只有管理员可以取消标记
        if (!user.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("只有管理员可以取消标记对话日志");
        }
        
        conversationLog.unflag();
        
        ConversationLog savedLog = conversationLogRepository.save(conversationLog);
        log.info("Conversation log {} unflagged", logId);
        
        return savedLog;
    }

    /**
     * 设置内容评级
     */
    @Transactional
    public ConversationLog setContentRating(Long logId, ConversationLog.ContentRating rating, Long userId) {
        log.info("Setting content rating for conversation log: {} to: {} by user: {}", logId, rating, userId);
        
        ConversationLog conversationLog = conversationLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("对话日志不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 权限检查：只有管理员和开发者可以设置内容评级
        if (!user.getRole().equals(User.UserRole.ADMIN) && 
            !user.getRole().equals(User.UserRole.DEVELOPER)) {
            throw new RuntimeException("只有管理员和开发者可以设置内容评级");
        }
        
        conversationLog.setContentRating(rating);
        
        ConversationLog savedLog = conversationLogRepository.save(conversationLog);
        log.info("Content rating set for conversation log {}", logId);
        
        return savedLog;
    }

    /**
     * 终止对话日志
     */
    @Transactional
    public ConversationLog terminateConversationLog(Long logId, String reason, Long userId) {
        log.info("Terminating conversation log: {} by user: {}", logId, userId);
        
        ConversationLog conversationLog = conversationLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("对话日志不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 权限检查：只有管理员可以终止对话
        if (!user.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("只有管理员可以终止对话");
        }
        
        conversationLog.terminate(reason);
        
        ConversationLog savedLog = conversationLogRepository.save(conversationLog);
        log.info("Conversation log {} terminated", logId);
        
        return savedLog;
    }

    /**
     * 归档对话日志
     */
    @Transactional
    public ConversationLog archiveConversationLog(Long logId, Long userId) {
        log.info("Archiving conversation log: {} by user: {}", logId, userId);
        
        ConversationLog conversationLog = conversationLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("对话日志不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 权限检查：只有管理员可以归档
        if (!user.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("只有管理员可以归档对话日志");
        }
        
        conversationLog.archive();
        
        ConversationLog savedLog = conversationLogRepository.save(conversationLog);
        log.info("Conversation log {} archived", logId);
        
        return savedLog;
    }

    /**
     * 获取对话日志详情
     */
    public Optional<ConversationLog> getConversationLogById(Long logId) {
        return conversationLogRepository.findById(logId);
    }

    /**
     * 根据会话ID获取对话日志
     */
    public Optional<ConversationLog> getConversationLogBySessionId(String sessionId) {
        return conversationLogRepository.findBySessionId(sessionId);
    }

    /**
     * 获取需要审核的对话日志
     */
    public Page<ConversationLog> getLogsNeedingReview(Pageable pageable) {
        return conversationLogRepository.findLogsNeedingReview(pageable);
    }

    /**
     * 获取被标记的对话日志
     */
    public Page<ConversationLog> getFlaggedLogs(Pageable pageable) {
        return conversationLogRepository.findByIsFlaggedTrue(pageable);
    }

    /**
     * 获取活跃的对话日志
     */
    public Page<ConversationLog> getActiveLogs(Pageable pageable) {
        return conversationLogRepository.findActiveLogs(pageable);
    }

    /**
     * 根据状态获取对话日志
     */
    public Page<ConversationLog> getLogsByStatus(ConversationLog.LogStatus status, Pageable pageable) {
        return conversationLogRepository.findByStatus(status, pageable);
    }

    /**
     * 根据内容评级获取对话日志
     */
    public Page<ConversationLog> getLogsByContentRating(ConversationLog.ContentRating rating, Pageable pageable) {
        return conversationLogRepository.findByContentRating(rating, pageable);
    }

    /**
     * 搜索对话日志
     */
    public Page<ConversationLog> searchLogs(String keyword, Pageable pageable) {
        return conversationLogRepository.searchLogs(keyword, pageable);
    }

    /**
     * 获取最近的对话日志
     */
    public Page<ConversationLog> getRecentLogs(Pageable pageable) {
        return conversationLogRepository.findRecentLogs(pageable);
    }

    /**
     * 获取用户的对话日志
     */
    public List<ConversationLog> getUserLogs(Long userId) {
        return conversationLogRepository.findByUserId(userId);
    }

    /**
     * 获取智能体的对话日志
     */
    public List<ConversationLog> getAgentLogs(Long agentId) {
        return conversationLogRepository.findByAgentId(agentId);
    }

    /**
     * 获取指定时间范围内的对话日志
     */
    public Page<ConversationLog> getLogsByDateRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return conversationLogRepository.findByCreatedAtBetween(startTime, endTime, pageable);
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 状态统计
        List<Object[]> statusStats = conversationLogRepository.countByStatus();
        Map<String, Long> statusCounts = new HashMap<>();
        for (Object[] stat : statusStats) {
            statusCounts.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("statusCounts", statusCounts);
        
        // 内容评级统计
        List<Object[]> ratingStats = conversationLogRepository.countByContentRating();
        Map<String, Long> ratingCounts = new HashMap<>();
        for (Object[] stat : ratingStats) {
            ratingCounts.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("ratingCounts", ratingCounts);
        
        // 总成本和token统计
        Double totalCost = conversationLogRepository.getTotalCost();
        Long totalTokens = conversationLogRepository.getTotalTokens();
        stats.put("totalCost", totalCost != null ? totalCost : 0.0);
        stats.put("totalTokens", totalTokens != null ? totalTokens : 0L);
        
        return stats;
    }

    /**
     * 获取指定时间范围内的统计信息
     */
    public Map<String, Object> getStatisticsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        Double totalCost = conversationLogRepository.getTotalCostBetween(startTime, endTime);
        Long totalTokens = conversationLogRepository.getTotalTokensBetween(startTime, endTime);
        
        stats.put("totalCost", totalCost != null ? totalCost : 0.0);
        stats.put("totalTokens", totalTokens != null ? totalTokens : 0L);
        
        return stats;
    }

    /**
     * 清理长时间无活动的对话日志
     */
    @Transactional
    public void cleanupInactiveLogs(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        List<ConversationLog> inactiveLogs = conversationLogRepository.findInactiveLogs(cutoffTime);
        
        for (ConversationLog log : inactiveLogs) {
            log.complete();
            conversationLogRepository.save(log);
        }
        
        log.info("Cleaned up {} inactive conversation logs", inactiveLogs.size());
    }

    /**
     * 清理旧的已归档对话日志
     */
    @Transactional
    public void cleanupOldArchivedLogs(int days) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        conversationLogRepository.deleteOldArchivedLogs(cutoffTime);
        log.info("Cleaned up old archived conversation logs before: {}", cutoffTime);
    }

    /**
     * 估算token数量的简单方法
     */
    private int estimateTokens(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        
        // 简单估算：中文字符数 * 1.5，英文单词数 * 1.3
        int chineseChars = 0;
        int englishWords = 0;
        
        for (char c : content.toCharArray()) {
            if (c >= 0x4e00 && c <= 0x9fff) {
                chineseChars++;
            }
        }
        
        String[] words = content.replaceAll("[\u4e00-\u9fff]", "").split("\\s+");
        englishWords = words.length;
        
        return (int) (chineseChars * 1.5 + englishWords * 1.3);
    }
}