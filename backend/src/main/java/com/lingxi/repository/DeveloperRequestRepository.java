package com.lingxi.repository;

import com.lingxi.entity.DeveloperRequest;
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
 * 开发者申请Repository接口
 */
@Repository
public interface DeveloperRequestRepository extends JpaRepository<DeveloperRequest, Long> {

    /**
     * 根据用户查找申请记录
     */
    List<DeveloperRequest> findByUser(User user);

    /**
     * 根据用户ID查找申请记录
     */
    List<DeveloperRequest> findByUserId(Long userId);

    /**
     * 根据用户ID查找申请记录（分页）
     */
    @Query(value = "SELECT dr FROM DeveloperRequest dr WHERE dr.user.id = :userId",
           countQuery = "SELECT count(dr) FROM DeveloperRequest dr WHERE dr.user.id = :userId")
    Page<DeveloperRequest> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据状态查找申请记录
     */
    List<DeveloperRequest> findByStatus(DeveloperRequest.RequestStatus status);

    /**
     * 分页查找待审核的申请
     */
    @Query(value = "SELECT dr FROM DeveloperRequest dr WHERE dr.status = :status",
           countQuery = "SELECT count(dr) FROM DeveloperRequest dr WHERE dr.status = :status")
    Page<DeveloperRequest> findByStatus(@Param("status") DeveloperRequest.RequestStatus status, Pageable pageable);

    /**
     * 查找用户的最新申请
     */
    Optional<DeveloperRequest> findTopByUserOrderByCreatedAtDesc(User user);

    /**
     * 查找用户的最新申请（按用户ID）
     */
    Optional<DeveloperRequest> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 检查用户是否有待审核的申请
     */
    boolean existsByUserAndStatus(User user, DeveloperRequest.RequestStatus status);

    /**
     * 检查用户是否有待审核的申请（按用户ID）
     */
    boolean existsByUserIdAndStatus(Long userId, DeveloperRequest.RequestStatus status);

    /**
     * 根据审核者查找申请记录
     */
    List<DeveloperRequest> findByReviewer(User reviewer);

    /**
     * 根据审核者ID查找申请记录
     */
    List<DeveloperRequest> findByReviewerId(Long reviewerId);

    /**
     * 根据审核者ID查找申请记录（分页）
     */
    @Query(value = "SELECT dr FROM DeveloperRequest dr WHERE dr.reviewer.id = :reviewerId",
           countQuery = "SELECT count(dr) FROM DeveloperRequest dr WHERE dr.reviewer.id = :reviewerId")
    Page<DeveloperRequest> findByReviewerId(@Param("reviewerId") Long reviewerId, Pageable pageable);

    /**
     * 查找指定时间范围内的申请
     */
    @Query("SELECT dr FROM DeveloperRequest dr WHERE dr.createdAt BETWEEN :startTime AND :endTime")
    List<DeveloperRequest> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定时间范围内的申请（分页）
     */
    @Query("SELECT dr FROM DeveloperRequest dr WHERE dr.createdAt BETWEEN :startTime AND :endTime")
    Page<DeveloperRequest> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                                  @Param("endTime") LocalDateTime endTime,
                                                  Pageable pageable);

    /**
     * 统计各状态的申请数量
     */
    @Query("SELECT dr.status, COUNT(dr) FROM DeveloperRequest dr GROUP BY dr.status")
    List<Object[]> countByStatus();

    /**
     * 查找待审核的申请
     */
    @Query("SELECT dr FROM DeveloperRequest dr LEFT JOIN FETCH dr.user LEFT JOIN FETCH dr.reviewer WHERE dr.status = 'PENDING' ORDER BY dr.createdAt DESC")
    List<DeveloperRequest> findPendingRequests();

    /**
     * 分页查找待审核的申请
     */
    @Query(value = "SELECT dr FROM DeveloperRequest dr WHERE dr.status = 'PENDING' ORDER BY dr.createdAt DESC",
           countQuery = "SELECT count(dr) FROM DeveloperRequest dr WHERE dr.status = 'PENDING'")
    Page<DeveloperRequest> findPendingRequests(Pageable pageable);

    /**
     * 查找超过指定天数未处理的申请
     */
    @Query("SELECT dr FROM DeveloperRequest dr WHERE dr.status = 'PENDING' AND dr.createdAt < :cutoffTime")
    List<DeveloperRequest> findOverdueRequests(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 根据关键词搜索申请（搜索理由、技能、经验、联系方式、用户名等）
     */
    @Query(value = "SELECT dr FROM DeveloperRequest dr WHERE " +
           "LOWER(dr.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dr.skills) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dr.experience) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dr.contactInfo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dr.user.username) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT count(dr) FROM DeveloperRequest dr WHERE " +
           "LOWER(dr.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dr.skills) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dr.experience) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dr.contactInfo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dr.user.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<DeveloperRequest> searchRequests(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计用户的申请次数
     */
    @Query("SELECT COUNT(dr) FROM DeveloperRequest dr WHERE dr.user = :user")
    long countByUser(@Param("user") User user);

    /**
     * 统计用户的申请次数（按用户ID）
     */
    @Query("SELECT COUNT(dr) FROM DeveloperRequest dr WHERE dr.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 查找最近的申请记录
     */
    @Query("SELECT dr FROM DeveloperRequest dr ORDER BY dr.createdAt DESC")
    Page<DeveloperRequest> findRecentRequests(Pageable pageable);

    /**
     * 删除指定时间之前的已处理申请记录（用于数据清理）
     */
    @Query("DELETE FROM DeveloperRequest dr WHERE dr.status IN ('APPROVED', 'REJECTED', 'CANCELLED') AND dr.updatedAt < :cutoffTime")
    void deleteOldProcessedRequests(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 统计指定状态和时间范围内的申请数量
     */
    @Query("SELECT COUNT(dr) FROM DeveloperRequest dr WHERE dr.status = :status AND dr.createdAt BETWEEN :startTime AND :endTime")
    long countByStatusAndCreatedAtBetween(@Param("status") DeveloperRequest.RequestStatus status,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的申请数量
     */
    @Query("SELECT COUNT(dr) FROM DeveloperRequest dr WHERE dr.createdAt BETWEEN :startTime AND :endTime")
    long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定状态的申请数量
     */
    long countByStatus(DeveloperRequest.RequestStatus status);

    /**
     * 查找所有申请记录（按创建时间倒序）
     */
    Page<DeveloperRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
}