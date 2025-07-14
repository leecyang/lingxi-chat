package com.lingxi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证入口点
 * 处理未认证的请求
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // 检查是否为公开端点
        boolean isPublicEndpoint = "/api/auth/login".equals(path) ||
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
                path.startsWith("/ws/");
        
        // 对于公开端点，不应该触发AuthenticationEntryPoint
        // 如果触发了，说明配置有问题，但我们直接放行
        if (isPublicEndpoint) {
            log.warn("Public endpoint {} {} triggered AuthenticationEntryPoint - this should not happen", method, path);
            // 对于公开端点，直接放行，不返回401
            return;
        }
        
        log.warn("Unauthorized access attempt: {} {}", method, path);
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "访问被拒绝，请先登录");
        errorResponse.put("path", request.getRequestURI());
        
        // 添加更详细的错误信息
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            errorResponse.put("details", "缺少Authorization头");
        } else if (!authHeader.startsWith("Bearer ")) {
            errorResponse.put("details", "Authorization头格式错误");
        } else {
            errorResponse.put("details", "Token无效或已过期");
        }

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}