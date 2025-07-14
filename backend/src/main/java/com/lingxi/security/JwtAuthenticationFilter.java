package com.lingxi.security;

import com.lingxi.entity.User;
import com.lingxi.repository.UserRepository;
import com.lingxi.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * JWT认证过滤器
 * 处理每个请求的JWT验证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            
            if (token != null && jwtUtil.validateToken(token)) {
                authenticateUser(token, request);
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            // 清除安全上下文
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取JWT token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 也可以从查询参数中获取token（用于WebSocket连接）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }

    /**
     * 认证用户
     */
    private void authenticateUser(String token, HttpServletRequest request) {
        try {
            Claims claims = jwtUtil.getClaimsFromToken(token);
            String username = claims.getSubject();
            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 验证用户是否存在且状态正常
                Optional<User> userOpt = userRepository.findByUsername(username);
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // 检查用户状态
                    if (user.getStatus() != User.UserStatus.ACTIVE) {
                        log.warn("User {} is not active, status: {}", username, user.getStatus());
                        return;
                    }
                    
                    // 检查用户ID是否匹配
                    if (!user.getId().equals(userId)) {
                        log.warn("User ID mismatch in token for user: {}", username);
                        return;
                    }
                    
                    // 检查角色是否匹配
                    if (!user.getRole().name().equals(role)) {
                        log.warn("Role mismatch in token for user: {}", username);
                        return;
                    }
                    
                    // 创建认证对象
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                    );
                    
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("User {} authenticated successfully with role {}", username, role);
                } else {
                    log.warn("User {} not found in database", username);
                }
            }
        } catch (Exception e) {
            log.error("Error authenticating user from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 跳过某些路径的JWT验证
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("Checking shouldNotFilter for {} {}", method, path);
        
        // 跳过公开接口 - 使用更宽松的匹配
        boolean shouldSkip =
               "/api/auth/login".equals(path) ||
               "/api/auth/register".equals(path) ||
               "/api/auth/refresh".equals(path) ||
               "/api/auth/validate".equals(path) ||
               path.startsWith("/api/auth/check-") ||
               path.startsWith("/api/auth/send-email") ||
               path.startsWith("/api/auth/verify-email") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/static/") ||
               path.startsWith("/public/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/ws/"); // WebSocket连接
               
        if (shouldSkip) {
            log.info("Skipping JWT filter for {} {} - shouldSkip: {}", method, path, shouldSkip);
        } else {
            log.debug("JWT filter will process {} {}", method, path);
        }
        
        return shouldSkip;
    }
}