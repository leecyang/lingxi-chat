package com.lingxi.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 认证响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UserInfo user;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String message;

    /**
     * 用户信息DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private String role;
        private String status;
        private Boolean emailVerified;
        private String phoneNumber;
        private String organization;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginTime;
    }

    /**
     * 创建成功响应
     */
    public static AuthResponse success(UserInfo user, String accessToken, String refreshToken, String tokenType, Long expiresIn) {
        return AuthResponse.builder()
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .message("认证成功")
                .build();
    }

    /**
     * 创建失败响应
     */
    public static AuthResponse failure(String message) {
        return AuthResponse.builder()
                .message(message)
                .build();
    }
}