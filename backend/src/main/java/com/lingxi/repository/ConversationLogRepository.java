package com.lingxi.repository;

import com.lingxi.entity.ConversationLog;
import com.lingxi.entity.User;
import com.lingxi.entity.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 对话日志Repository接口
 */
@Repository
public interface ConversationLogRepository extends JpaRepository<ConversationLog, Long> {

    /**
     * 根据用户查找对话日志
     */
    List<ConversationLog> findByUser(User user);

    /**
     * 根据用户ID查找对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl LEFT JOIN FETCH cl.user LEFT JOIN FETCH cl.agent LEFT JOIN FETCH cl.flaggedBy WHERE cl.user.id = :userId ORDER BY cl.createdAt DESC")
    List<ConversationLog> findByUserId(@Param("userId") Long userId);

    /**
     * 根据智能体查找对话日志
     */
    List<ConversationLog> findByAgent(Agent agent);

    /**
     * 根据智能体ID查找对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl LEFT JOIN FETCH cl.user LEFT JOIN FETCH cl.agent LEFT JOIN FETCH cl.flaggedBy WHERE cl.agent.id = :agentId ORDER BY cl.createdAt DESC")
    List<ConversationLog> findByAgentId(@Param("agentId") Long agentId);

    /**
     * 根据会话ID查找对话日志
     */
    Optional<ConversationLog> findBySessionId(String sessionId);

    /**
     * 根据状态查找对话日志
     */
    List<ConversationLog> findByStatus(ConversationLog.LogStatus status);

    /**
     * 分页查找指定状态的对话日志
     */
    Page<ConversationLog> findByStatus(ConversationLog.LogStatus status, Pageable pageable);

    /**
     * 查找被标记的对话日志
     */
    List<ConversationLog> findByIsFlaggedTrue();

    /**
     * 分页查找被标记的对话日志
     */
    Page<ConversationLog> findByIsFlaggedTrue(Pageable pageable);

    /**
     * 根据内容评级查找对话日志
     */
    List<ConversationLog> findByContentRating(ConversationLog.ContentRating contentRating);

    /**
     * 分页查找指定内容评级的对话日志
     */
    Page<ConversationLog> findByContentRating(ConversationLog.ContentRating contentRating, Pageable pageable);

    /**
     * 查找需要审核的对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl LEFT JOIN FETCH cl.user LEFT JOIN FETCH cl.agent LEFT JOIN FETCH cl.flaggedBy WHERE cl.isFlagged = true OR cl.contentRating IN ('QUESTIONABLE', 'INAPPROPRIATE', 'HARMFUL')")
    List<ConversationLog> findLogsNeedingReview();

    /**
     * 分页查找需要审核的对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl LEFT JOIN FETCH cl.user LEFT JOIN FETCH cl.agent LEFT JOIN FETCH cl.flaggedBy WHERE cl.isFlagged = true OR cl.contentRating IN ('QUESTIONABLE', 'INAPPROPRIATE', 'HARMFUL')")
    Page<ConversationLog> findLogsNeedingReview(Pageable pageable);

    /**
     * 根据用户和智能体查找对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl LEFT JOIN FETCH cl.user LEFT JOIN FETCH cl.agent LEFT JOIN FETCH cl.flaggedBy WHERE cl.user = :user AND cl.agent = :agent ORDER BY cl.createdAt DESC")
    List<ConversationLog> findByUserAndAgent(@Param("user") User user, @Param("agent") Agent agent);

    /**
     * 根据用户ID和智能体ID查找对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl LEFT JOIN FETCH cl.user LEFT JOIN FETCH cl.agent LEFT JOIN FETCH cl.flaggedBy WHERE cl.user.id = :userId AND cl.agent.id = :agentId ORDER BY cl.createdAt DESC")
    List<ConversationLog> findByUserIdAndAgentId(@Param("userId") Long userId, @Param("agentId") Long agentId);

    /**
     * 查找指定时间范围内的对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl WHERE cl.createdAt BETWEEN :startTime AND :endTime")
    List<ConversationLog> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 分页查找指定时间范围内的对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl WHERE cl.createdAt BETWEEN :startTime AND :endTime")
    Page<ConversationLog> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime, 
                                                Pageable pageable);

    /**
     * 统计各状态的对话日志数量
     */
    @Query("SELECT cl.status, COUNT(cl) FROM ConversationLog cl GROUP BY cl.status")
    List<Object[]> countByStatus();

    /**
     * 统计各内容评级的对话日志数量
     */
    @Query("SELECT cl.contentRating, COUNT(cl) FROM ConversationLog cl WHERE cl.contentRating IS NOT NULL GROUP BY cl.contentRating")
    List<Object[]> countByContentRating();

    /**
     * 统计用户的对话次数
     */
    @Query("SELECT COUNT(cl) FROM ConversationLog cl WHERE cl.user = :user")
    long countByUser(@Param("user") User user);

    /**
     * 统计用户的对话次数（按用户ID）
     */
    @Query("SELECT COUNT(cl) FROM ConversationLog cl WHERE cl.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 统计智能体的使用次数
     */
    @Query("SELECT COUNT(cl) FROM ConversationLog cl WHERE cl.agent = :agent")
    long countByAgent(@Param("agent") Agent agent);

    /**
     * 统计智能体的使用次数（按智能体ID）
     */
    @Query("SELECT COUNT(cl) FROM ConversationLog cl WHERE cl.agent.id = :agentId")
    long countByAgentId(@Param("agentId") Long agentId);

    /**
     * 查找活跃的对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl LEFT JOIN FETCH cl.user LEFT JOIN FETCH cl.agent LEFT JOIN FETCH cl.flaggedBy WHERE cl.status = 'ACTIVE' ORDER BY cl.lastActivityAt DESC")
    List<ConversationLog> findActiveLogs();

    /**
     * 分页查找活跃的对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl LEFT JOIN FETCH cl.user LEFT JOIN FETCH cl.agent LEFT JOIN FETCH cl.flaggedBy WHERE cl.status = 'ACTIVE' ORDER BY cl.lastActivityAt DESC")
    Page<ConversationLog> findActiveLogs(Pageable pageable);

    /**
     * 查找长时间无活动的对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl WHERE cl.status = 'ACTIVE' AND cl.lastActivityAt < :cutoffTime")
    List<ConversationLog> findInactiveLogs(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 根据关键词搜索对话日志（搜索摘要、标记原因等字段）
     */
    @Query("SELECT cl FROM ConversationLog cl LEFT JOIN FETCH cl.user LEFT JOIN FETCH cl.agent LEFT JOIN FETCH cl.flaggedBy WHERE " +
           "LOWER(cl.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cl.flagReason) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cl.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cl.agent.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ConversationLog> searchLogs(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查找高成本的对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl WHERE cl.costAmount > :threshold ORDER BY cl.costAmount DESC")
    List<ConversationLog> findHighCostLogs(@Param("threshold") Double threshold);

    /**
     * 查找长时间对话的日志
     */
    @Query("SELECT cl FROM ConversationLog cl WHERE cl.durationMinutes > :threshold ORDER BY cl.durationMinutes DESC")
    List<ConversationLog> findLongConversations(@Param("threshold") Integer threshold);

    /**
     * 查找消息数量多的对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl WHERE cl.messageCount > :threshold ORDER BY cl.messageCount DESC")
    List<ConversationLog> findHighVolumeConversations(@Param("threshold") Integer threshold);

    /**
     * 统计总的对话成本
     */
    @Query("SELECT SUM(cl.costAmount) FROM ConversationLog cl WHERE cl.costAmount IS NOT NULL")
    Double getTotalCost();

    /**
     * 统计指定时间范围内的总成本
     */
    @Query("SELECT SUM(cl.costAmount) FROM ConversationLog cl WHERE cl.costAmount IS NOT NULL AND cl.createdAt BETWEEN :startTime AND :endTime")
    Double getTotalCostBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计总的token使用量
     */
    @Query("SELECT SUM(cl.totalTokens) FROM ConversationLog cl WHERE cl.totalTokens IS NOT NULL")
    Long getTotalTokens();

    /**
     * 统计指定时间范围内的token使用量
     */
    @Query("SELECT SUM(cl.totalTokens) FROM ConversationLog cl WHERE cl.totalTokens IS NOT NULL AND cl.createdAt BETWEEN :startTime AND :endTime")
    Long getTotalTokensBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的已归档对话日志（用于数据清理）
     */
    @Query("DELETE FROM ConversationLog cl WHERE cl.status = 'ARCHIVED' AND cl.updatedAt < :cutoffTime")
    void deleteOldArchivedLogs(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 查找最近的对话日志
     */
    @Query("SELECT cl FROM ConversationLog cl LEFT JOIN FETCH cl.user LEFT JOIN FETCH cl.agent LEFT JOIN FETCH cl.flaggedBy ORDER BY cl.createdAt DESC")
    Page<ConversationLog> findRecentLogs(Pageable pageable);
}