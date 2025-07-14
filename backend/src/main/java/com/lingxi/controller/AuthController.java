package com.lingxi.controller;

import com.lingxi.dto.AuthResponse;
import com.lingxi.dto.LoginRequest;
import com.lingxi.dto.RegisterRequest;
import com.lingxi.entity.User;
import com.lingxi.service.AuthService;
import com.lingxi.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 生产环境应该限制具体域名
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;


    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("=== REGISTER ENDPOINT REACHED ====");
        log.info("User registration attempt: {}", request.getUsername());
        log.info("Request details: email={}, nickname={}", request.getEmail(), request.getNickname());
        
        try {
            // 验证密码确认
            if (!request.isPasswordConfirmed()) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.failure("密码确认不匹配"));
            }
            
            // 验证同意条款
            if (!Boolean.TRUE.equals(request.getAgreeToTerms())) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.failure("必须同意服务条款"));
            }
            
            // 注册用户
            User user = authService.register(request);
            
            // 生成token
            JwtUtil.TokenResponse tokenResponse = jwtUtil.generateTokenResponse(user);
            
            // 构建用户信息
            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .role(user.getRole().name())
                    .status(user.getStatus().name())
                    .emailVerified(user.getEmailVerified())
                    .phoneNumber(user.getPhoneNumber())
                    .organization(user.getOrganization())
                    .createdAt(user.getCreatedAt())
                    .lastLoginTime(user.getLastLoginTime())
                    .build();
            
            AuthResponse response = AuthResponse.success(
                    userInfo,
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    "Bearer",
                    tokenResponse.getExpiresIn()
            );
            
            log.info("User registered successfully: {}", user.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Registration failed for user: {}", request.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(AuthResponse.failure(e.getMessage()));
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());
        
        try {
            // 登录验证
            User user = authService.login(request);
            
            // 生成token
            JwtUtil.TokenResponse tokenResponse = jwtUtil.generateTokenResponse(user);
            
            // 构建用户信息
            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .role(user.getRole().name())
                    .status(user.getStatus().name())
                    .emailVerified(user.getEmailVerified())
                    .phoneNumber(user.getPhoneNumber())
                    .organization(user.getOrganization())
                    .createdAt(user.getCreatedAt())
                    .lastLoginTime(user.getLastLoginTime())
                    .build();
            
            AuthResponse response = AuthResponse.success(
                    userInfo,
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    "Bearer",
                    tokenResponse.getExpiresIn()
            );
            
            log.info("User logged in successfully: {}", user.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(AuthResponse.failure(e.getMessage()));
        }
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "刷新token不能为空"));
            }
            
            JwtUtil.TokenResponse tokenResponse = authService.refreshToken(refreshToken);
            
            return ResponseEntity.ok(Map.of(
                    "accessToken", tokenResponse.getAccessToken(),
                    "refreshToken", tokenResponse.getRefreshToken(),
                    "tokenType", "Bearer",
                    "expiresIn", tokenResponse.getExpiresIn()
            ));
            
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                authService.logout(token);
            }
            
            return ResponseEntity.ok(Map.of("message", "登出成功"));
            
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证头"));
            }
            
            String token = authHeader.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的token"));
            }
            
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "旧密码和新密码不能为空"));
            }
            
            authService.changePassword(userId, oldPassword, newPassword);
            
            return ResponseEntity.ok(Map.of("message", "密码修改成功"));
            
        } catch (Exception e) {
            log.error("Password change failed", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 验证token
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("valid", false, "error", "无效的认证头"));
            }
            
            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.validateToken(token);
            
            if (isValid) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "userId", userId,
                        "username", username,
                        "role", role
                ));
            } else {
                return ResponseEntity.ok(Map.of("valid", false));
            }
            
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "error", e.getMessage()));
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证头"));
            }
            
            String token = authHeader.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的token"));
            }
            
            User user = authService.getCurrentUser(userId);
            
            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .role(user.getRole().name())
                    .status(user.getStatus().name())
                    .emailVerified(user.getEmailVerified())
                    .phoneNumber(user.getPhoneNumber())
                    .organization(user.getOrganization())
                    .createdAt(user.getCreatedAt())
                    .lastLoginTime(user.getLastLoginTime())
                    .build();
            
            return ResponseEntity.ok(Map.of("user", userInfo));
            
        } catch (Exception e) {
            log.error("Get current user failed", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        try {
            boolean available = authService.isUsernameAvailable(username);
            return ResponseEntity.ok(Map.of("available", available));
        } catch (Exception e) {
            log.error("Check username failed", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("available", false));
        }
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        try {
            boolean available = authService.isEmailAvailable(email);
            return ResponseEntity.ok(Map.of("available", available));
        } catch (Exception e) {
            log.error("Check email failed", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("available", false));
        }
    }

    /**
     * 发送邮箱验证
     */
    @PostMapping("/send-verification")
    public ResponseEntity<Map<String, String>> sendEmailVerification(
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证头"));
            }
            
            String token = authHeader.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的token"));
            }
            
            // TODO: 实现邮箱验证发送逻辑
            // authService.sendEmailVerification(userId);
            
            return ResponseEntity.ok(Map.of("message", "验证邮件已发送"));
            
        } catch (Exception e) {
            log.error("Send email verification failed", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 验证邮箱
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody Map<String, String> request) {
        try {
            String verificationCode = request.get("code");
            String email = request.get("email");
            
            if (verificationCode == null || email == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "验证码和邮箱不能为空"));
            }
            
            // TODO: 实现邮箱验证逻辑
            // authService.verifyEmail(email, verificationCode);
            
            return ResponseEntity.ok(Map.of("message", "邮箱验证成功"));
            
        } catch (Exception e) {
            log.error("Email verification failed", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}