package com.lingxi.repository;

import com.lingxi.entity.AgentSubmission;
import com.lingxi.entity.User;
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
 * 智能体提交申请数据访问层
 */
@Repository
public interface AgentSubmissionRepository extends JpaRepository<AgentSubmission, Long> {

    /**
     * 根据提交者查找申请
     */
    List<AgentSubmission> findBySubmitter(User submitter);

    /**
     * 根据提交者和状态查找申请
     */
    List<AgentSubmission> findBySubmitterAndStatus(User submitter, AgentSubmission.SubmissionStatus status);

    /**
     * 根据状态查找申请（分页）
     */
    Page<AgentSubmission> findByStatus(AgentSubmission.SubmissionStatus status, Pageable pageable);

    /**
     * 根据状态查找申请
     */
    List<AgentSubmission> findByStatus(AgentSubmission.SubmissionStatus status);

    /**
     * 根据审核者查找申请
     */
    List<AgentSubmission> findByReviewer(User reviewer);

    /**
     * 根据智能体名称查找申请
     */
    Optional<AgentSubmission> findByName(String name);

    /**
     * 根据类别查找申请
     */
    List<AgentSubmission> findByCategory(String category);

    /**
     * 查找指定时间范围内的申请
     */
    @Query("SELECT a FROM AgentSubmission a WHERE a.createdAt BETWEEN :startTime AND :endTime")
    List<AgentSubmission> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各状态的申请数量
     */
    @Query("SELECT a.status, COUNT(a) FROM AgentSubmission a GROUP BY a.status")
    List<Object[]> countByStatus();

    /**
     * 统计指定用户的申请数量
     */
    long countBySubmitter(User submitter);

    /**
     * 统计指定用户待审核的申请数量
     */
    long countBySubmitterAndStatus(User submitter, AgentSubmission.SubmissionStatus status);

    /**
     * 查找最近的申请（分页）
     */
    Page<AgentSubmission> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 根据提交者角色查找申请
     */
    List<AgentSubmission> findBySubmitterRole(User.UserRole submitterRole);

    /**
     * 检查用户是否已提交过同名的智能体申请
     */
    boolean existsBySubmitterAndName(User submitter, String name);

    /**
     * 检查是否存在同名的待审核申请
     */
    boolean existsByNameAndStatus(String name, AgentSubmission.SubmissionStatus status);
}