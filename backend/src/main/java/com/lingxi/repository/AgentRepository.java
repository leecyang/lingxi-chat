package com.lingxi.repository;

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
 * 智能体数据访问接口
 */
@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    /**
     * 根据名称查找智能体
     */
    Optional<Agent> findByName(String name);

    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据模型ID查找智能体
     */
    List<Agent> findByModelId(String modelId);

    /**
     * 根据状态查找智能体
     */
    List<Agent> findByStatus(Agent.AgentStatus status);

    /**
     * 根据类型查找智能体
     */
    List<Agent> findByType(Agent.AgentType type);

    /**
     * 根据创建者查找智能体
     */
    List<Agent> findByCreator_Id(Long creatorId);

    /**
     * 查找已批准的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.status = 'APPROVED' ORDER BY a.priority DESC, a.createdAt ASC")
    List<Agent> findApprovedAgents();

    /**
     * 分页查找已批准的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.status = 'APPROVED' ORDER BY a.priority DESC, a.createdAt ASC")
    Page<Agent> findApprovedAgents(Pageable pageable);

    /**
     * 查找待审核的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.status = 'PENDING' ORDER BY a.createdAt ASC")
    List<Agent> findPendingAgents();
    
    /**
     * 分页查找待审核的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.status = 'PENDING' ORDER BY a.createdAt ASC")
    Page<Agent> findPendingAgents(Pageable pageable);

    /**
     * 查找活跃的智能体（已批准且启用）
     */
    @Query("SELECT a FROM Agent a WHERE a.status = 'APPROVED' AND a.enabled = true ORDER BY a.priority DESC")
    List<Agent> findActiveAgents();

    /**
     * 根据创建者和状态查找智能体
     */
    Page<Agent> findByCreator_IdAndStatus(Long creatorId, Agent.AgentStatus status, Pageable pageable);

    /**
     * 根据类型和状态查找智能体
     */
    List<Agent> findByTypeAndStatus(Agent.AgentType type, Agent.AgentStatus status);

    /**
     * 模糊查询智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.name LIKE %:keyword% OR a.description LIKE %:keyword% OR a.modelId LIKE %:keyword%")
    Page<Agent> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查找最近创建的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Agent> findRecentAgents(@Param("since") LocalDateTime since);

    /**
     * 查找最近更新的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.updatedAt >= :since ORDER BY a.updatedAt DESC")
    List<Agent> findRecentlyUpdatedAgents(@Param("since") LocalDateTime since);

    /**
     * 统计智能体数量按状态
     */
    @Query("SELECT a.status, COUNT(a) FROM Agent a GROUP BY a.status")
    List<Object[]> countAgentsByStatus();

    /**
     * 统计智能体数量按类型
     */
    @Query("SELECT a.type, COUNT(a) FROM Agent a GROUP BY a.type")
    List<Object[]> countAgentsByType();



    /**
     * 查找特定创建者的活跃智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.creator.id = :creatorId AND a.status = 'APPROVED' AND a.enabled = true")
    List<Agent> findActiveAgentsByCreator(@Param("creatorId") Long creatorId);

    /**
     * 查找需要审核的智能体（按优先级排序）
     */
    @Query("SELECT a FROM Agent a WHERE a.status = 'PENDING' ORDER BY a.priority DESC, a.createdAt ASC")
    List<Agent> findAgentsForReview();

    /**
     * 查找被拒绝的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.status = 'REJECTED' ORDER BY a.updatedAt DESC")
    List<Agent> findRejectedAgents();

    /**
     * 查找被禁用的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.enabled = false ORDER BY a.updatedAt DESC")
    List<Agent> findDisabledAgents();

    /**
     * 查找错误率高的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.totalCalls > 0 AND (a.totalCalls - a.successCalls) * 1.0 / a.totalCalls > :errorRate")
    List<Agent> findHighErrorRateAgents(@Param("errorRate") Double errorRate);

    /**
     * 查找响应时间慢的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.averageResponseTime > :threshold")
    List<Agent> findSlowResponseAgents(@Param("threshold") Long threshold);

    /**
     * 统计今日新增智能体数
     */
    @Query("SELECT COUNT(a) FROM Agent a WHERE a.createdAt >= :startOfDay")
    long countTodayCreatedAgents(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 统计今日调用次数
     */
    @Query("SELECT SUM(a.totalCalls) FROM Agent a")
    Long sumTodayCalls();

    /**
     * 查找特定端点的智能体
     */
    List<Agent> findByEndpoint(String endpoint);

    /**
     * 查找支持特定功能的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.description LIKE %:capability% AND a.status = 'APPROVED'")
    List<Agent> findAgentsByCapability(@Param("capability") String capability);

    /**
     * 查找配置中包含特定参数的智能体
     */
    @Query("SELECT DISTINCT a FROM Agent a, IN(a.config) c WHERE KEY(c) = :configKey")
    List<Agent> findAgentsByConfigKey(@Param("configKey") String configKey);

    /**
     * 查找智能体按优先级排序
     */
    List<Agent> findAllByOrderByPriorityDescCreatedAtAsc();

    /**
     * 查找智能体按创建时间排序
     */
    List<Agent> findAllByOrderByCreatedAtDesc();

    /**
     * 查找智能体按更新时间排序
     */
    List<Agent> findAllByOrderByUpdatedAtDesc();

    /**
     * 查找智能体按调用次数排序
     */
    List<Agent> findAllByOrderByTotalCallsDesc();

    /**
     * 查找智能体按优先级排序
     */
    List<Agent> findAllByOrderByPriorityDesc();
}