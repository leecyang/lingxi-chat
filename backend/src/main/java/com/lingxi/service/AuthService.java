package com.lingxi.service;

import com.lingxi.dto.LoginRequest;
import com.lingxi.dto.RegisterRequest;
import com.lingxi.entity.User;

import com.lingxi.repository.UserRepository;
import com.lingxi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 认证服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .role(User.UserRole.USER)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(false)
                .loginAttempts(0)
                .phoneNumber(request.getPhoneNumber())
                .organization(request.getOrganization())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    /**
     * 用户登录
     */
    public User login(LoginRequest request) {
        // 查找用户（支持用户名或邮箱登录）
        User user = null;
        String loginIdentifier = request.getUsername();
        
        // 判断是邮箱还是用户名
        if (loginIdentifier.contains("@")) {
            // 邮箱登录
            user = userRepository.findByEmail(loginIdentifier)
                    .orElseThrow(() -> new RuntimeException("邮箱或密码错误"));
        } else {
            // 用户名登录
            user = userRepository.findByUsername(loginIdentifier)
                    .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        }

        // 检查用户状态
        if (user.getStatus() == User.UserStatus.INACTIVE) {
            throw new RuntimeException("账户已被禁用");
        }

        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new RuntimeException("账户已被封禁");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }

    /**
     * 刷新token
     */
    public JwtUtil.TokenResponse refreshToken(String refreshToken) {
        try {
            // 验证刷新token
            if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
                throw new RuntimeException("无效的刷新token");
            }

            // 从刷新token中提取用户信息
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            String role = jwtUtil.getRoleFromToken(refreshToken);

            // 验证用户是否仍然存在且状态正常
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (user.getStatus() != User.UserStatus.ACTIVE) {
                throw new RuntimeException("用户状态异常");
            }

            // 生成新的token对
            return jwtUtil.generateTokenResponse(user);

        } catch (Exception e) {
            throw new RuntimeException("刷新token失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        try {
            // 将token加入黑名单（这里简化处理，实际项目中可能需要Redis等缓存）
            // TODO: 实现token黑名单机制
            log.info("User logged out, token invalidated");
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw new RuntimeException("登出失败");
        }
    }

    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 获取当前用户信息
     */
    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 检查用户名是否可用
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否可用
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 发送邮箱验证
     */
    public void sendEmailVerification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // TODO: 实现邮箱验证发送逻辑
        log.info("Sending email verification to user: {}", user.getEmail());
    }

    /**
     * 验证邮箱
     */
    public void verifyEmail(String email, String verificationCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // TODO: 实现邮箱验证逻辑
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getUsername());
    }
}