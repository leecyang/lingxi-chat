package com.lingxi.repository;

import com.lingxi.entity.ChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 聊天历史数据访问接口
 */
@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    /**
     * 根据用户ID查找聊天历史
     */
    List<ChatHistory> findByUser_IdOrderByCreatedAtAsc(Long userId);

    /**
     * 根据用户ID和会话ID查找聊天历史
     */
    List<ChatHistory> findByUser_IdAndSessionIdOrderBySequenceNumberAsc(Long userId, String sessionId);

    /**
     * 根据用户ID和智能体ID查找聊天历史
     */
    List<ChatHistory> findByUser_IdAndAgent_IdOrderByCreatedAtAsc(Long userId, Long agentId);

    /**
     * 根据用户ID、智能体ID和会话ID查找聊天历史
     */
    List<ChatHistory> findByUser_IdAndAgent_IdAndSessionIdOrderBySequenceNumberAsc(Long userId, Long agentId, String sessionId);

    /**
     * 根据会话ID查找聊天历史
     */
    List<ChatHistory> findBySessionIdOrderBySequenceNumberAsc(String sessionId);

    /**
     * 根据智能体ID查找聊天历史
     */
    List<ChatHistory> findByAgent_IdOrderByCreatedAtDesc(Long agentId);

    /**
     * 根据消息类型查找聊天历史
     */
    List<ChatHistory> findByMessageType(ChatHistory.MessageType messageType);

    /**
     * 根据消息状态查找聊天历史
     */
    List<ChatHistory> findByStatus(ChatHistory.MessageStatus status);

    /**
     * 查找用户最近的聊天历史
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.user.id = :userId AND ch.createdAt >= :since ORDER BY ch.createdAt DESC")
    List<ChatHistory> findRecentChatHistory(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * 查找用户最新的会话
     */
    @Query(value = "SELECT DISTINCT ch.sessionId FROM ChatHistory ch WHERE ch.user.id = :userId GROUP BY ch.sessionId ORDER BY MAX(ch.createdAt) DESC", 
           countQuery = "SELECT COUNT(DISTINCT ch.sessionId) FROM ChatHistory ch WHERE ch.user.id = :userId")
    Page<String> findRecentSessionsByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找会话中的最后一条消息
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.sessionId = :sessionId ORDER BY ch.sequenceNumber DESC")
    List<ChatHistory> findLastMessageInSession(@Param("sessionId") String sessionId, Pageable pageable);

    /**
     * 查找用户在特定时间范围内的聊天历史
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.user.id = :userId AND ch.createdAt BETWEEN :startTime AND :endTime ORDER BY ch.createdAt DESC")
    List<ChatHistory> findByUser_IdAndTimeRange(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查找包含特定关键词的聊天历史
     */
   @Query("SELECT ch FROM ChatHistory ch WHERE ch.user.id = :userId AND (ch.content LIKE %:keyword% OR ch.rawContent LIKE %:keyword%) ORDER BY ch.createdAt DESC")
    Page<ChatHistory> findByUser_IdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 查找失败的消息
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.status = 'FAILED' ORDER BY ch.createdAt DESC")
    List<ChatHistory> findFailedMessages();

    /**
     * 根据用户ID和状态查找消息
     */
    Page<ChatHistory> findByUser_IdAndStatusIn(Long userId, List<ChatHistory.MessageStatus> statuses, Pageable pageable);

    /**
     * 查找处理中的消息
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.status = 'PROCESSING' ORDER BY ch.createdAt ASC")
    List<ChatHistory> findProcessingMessages();

    /**
     * 查找超时的消息
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.status = 'PROCESSING' AND ch.createdAt < :timeout")
    List<ChatHistory> findTimeoutMessages(@Param("timeout") LocalDateTime timeout);

    /**
     * 统计用户消息数量
     */
    @Query("SELECT COUNT(ch) FROM ChatHistory ch WHERE ch.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 统计智能体消息数量
     */
    @Query("SELECT COUNT(ch) FROM ChatHistory ch WHERE ch.agent.id = :agentId")
    long countByAgentId(@Param("agentId") Long agentId);

    /**
     * 统计会话消息数量
     */
    @Query("SELECT COUNT(ch) FROM ChatHistory ch WHERE ch.sessionId = :sessionId")
    long countBySessionId(@Param("sessionId") String sessionId);

    /**
     * 统计今日消息数量
     */
    @Query("SELECT COUNT(ch) FROM ChatHistory ch WHERE ch.createdAt >= :startOfDay")
    long countTodayMessages(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 统计用户今日消息数量
     */
    @Query("SELECT COUNT(ch) FROM ChatHistory ch WHERE ch.user.id = :userId AND ch.createdAt >= :startOfDay")
    long countTodayMessagesByUser(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 查找高评分的对话
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.userRating >= :minRating ORDER BY ch.userRating DESC, ch.createdAt DESC")
    List<ChatHistory> findHighRatedMessages(@Param("minRating") Integer minRating);

    /**
     * 查找有用的对话
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.isHelpful = true ORDER BY ch.createdAt DESC")
    List<ChatHistory> findHelpfulMessages();

    /**
     * 查找包含情绪分析的对话
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.emotionLabel IS NOT NULL ORDER BY ch.createdAt DESC")
    List<ChatHistory> findMessagesWithEmotion();

    /**
     * 查找特定情绪的对话
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.emotionLabel LIKE %:emotion% ORDER BY ch.createdAt DESC")
    List<ChatHistory> findMessagesByEmotion(@Param("emotion") String emotion);

    /**
     * 查找响应时间较长的对话
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.responseTimeMs > :threshold ORDER BY ch.responseTimeMs DESC")
    List<ChatHistory> findSlowResponseMessages(@Param("threshold") Long threshold);

    /**
     * 查找有错误的对话
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.errorMessage IS NOT NULL ORDER BY ch.createdAt DESC")
    List<ChatHistory> findMessagesWithErrors();

    /**
     * 查找父消息的所有子消息
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.parentMessageId = :parentId ORDER BY ch.sequenceNumber ASC")
    List<ChatHistory> findChildMessages(@Param("parentId") Long parentId);

    /**
     * 查找会话的根消息（没有父消息的消息）
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.sessionId = :sessionId AND ch.parentMessageId IS NULL ORDER BY ch.sequenceNumber ASC")
    List<ChatHistory> findRootMessagesInSession(@Param("sessionId") String sessionId);

    /**
     * 删除指定时间之前的聊天历史
     */
    @Modifying
    @Query("DELETE FROM ChatHistory ch WHERE ch.createdAt < :cutoffTime")
    int deleteOldChatHistory(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 删除用户的聊天历史
     */
    @Modifying
    @Query("DELETE FROM ChatHistory ch WHERE ch.user.id = :userId")
    int deleteChatHistoryByUserId(@Param("userId") Long userId);

    /**
     * 删除会话的聊天历史
     */
    @Modifying
    @Query("DELETE FROM ChatHistory ch WHERE ch.sessionId = :sessionId")
    int deleteChatHistoryBySessionId(@Param("sessionId") String sessionId);

    /**
     * 统计智能体的平均响应时间
     */
    @Query("SELECT AVG(ch.responseTimeMs) FROM ChatHistory ch WHERE ch.agent.id = :agentId AND ch.responseTimeMs IS NOT NULL")
    Double getAverageResponseTimeByAgent(@Param("agentId") Long agentId);

    /**
     * 统计智能体的平均评分
     */
    @Query("SELECT AVG(ch.userRating) FROM ChatHistory ch WHERE ch.agent.id = :agentId AND ch.userRating IS NOT NULL")
    Double getAverageRatingByAgent(@Param("agentId") Long agentId);

    /**
     * 查找最活跃的用户
     */
    @Query("SELECT ch.user.id, COUNT(ch) as messageCount FROM ChatHistory ch WHERE ch.createdAt >= :since GROUP BY ch.user.id ORDER BY messageCount DESC")
    List<Object[]> findMostActiveUsers(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 查找最受欢迎的智能体
     */
    @Query("SELECT ch.agent.id, COUNT(ch) as messageCount FROM ChatHistory ch WHERE ch.createdAt >= :since GROUP BY ch.agent.id ORDER BY messageCount DESC")
    List<Object[]> findMostPopularAgents(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 删除智能体的聊天历史
     */
    @Modifying
    @Query("DELETE FROM ChatHistory ch WHERE ch.agent.id = :agentId")
    int deleteChatHistoryByAgentId(@Param("agentId") Long agentId);
}