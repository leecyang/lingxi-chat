package com.lingxi.repository;

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
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据角色查找用户
     */
    List<User> findByRole(User.UserRole role);

    /**
     * 根据状态查找用户
     */
    List<User> findByStatus(User.UserStatus status);

    /**
     * 根据角色和状态查找用户
     */
    Page<User> findByRoleAndStatus(User.UserRole role, User.UserStatus status, Pageable pageable);

    /**
     * 查找活跃用户
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    List<User> findActiveUsers();

    /**
     * 查找被锁定的用户
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > :now")
    List<User> findLockedUsers(@Param("now") LocalDateTime now);

    /**
     * 查找需要解锁的用户
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil <= :now")
    List<User> findUsersToUnlock(@Param("now") LocalDateTime now);

    /**
     * 根据用户名或邮箱模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%")
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查找最近注册的用户
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(@Param("since") LocalDateTime since);

    /**
     * 查找最近登录的用户
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginTime >= :since ORDER BY u.lastLoginTime DESC")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);

    /**
     * 统计用户数量按角色
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    /**
     * 统计用户数量按状态
     */
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countUsersByStatus();

    /**
     * 查找邮箱未验证的用户
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false")
    List<User> findUnverifiedUsers();

    /**
     * 查找开发者用户
     */
    @Query("SELECT u FROM User u WHERE u.role = 'DEVELOPER' AND u.status = 'ACTIVE'")
    List<User> findActiveDevelopers();

    /**
     * 查找管理员用户
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.status = 'ACTIVE'")
    List<User> findActiveAdmins();

    /**
     * 根据组织查找用户
     */
    List<User> findByOrganization(String organization);

    /**
     * 查找登录失败次数过多的用户
     */
    @Query("SELECT u FROM User u WHERE u.loginAttempts >= :maxAttempts")
    List<User> findUsersWithExcessiveFailedLogins(@Param("maxAttempts") int maxAttempts);

    /**
     * 重置用户登录尝试次数
     */
    @Query("UPDATE User u SET u.loginAttempts = 0, u.lockedUntil = null WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil <= :now")
    int unlockExpiredUsers(@Param("now") LocalDateTime now);

    /**
     * 查找长时间未登录的用户
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginTime < :threshold OR u.lastLoginTime IS NULL")
    List<User> findInactiveUsers(@Param("threshold") LocalDateTime threshold);

    /**
     * 统计今日新注册用户数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfDay")
    long countTodayRegistrations(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 统计今日活跃用户数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginTime >= :startOfDay")
    long countTodayActiveUsers(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 查找用户按创建时间排序
     */
    List<User> findAllByOrderByCreatedAtDesc();

    /**
     * 查找用户按最后登录时间排序
     */
    List<User> findAllByOrderByLastLoginTimeDesc();
}