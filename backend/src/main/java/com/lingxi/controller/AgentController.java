package com.lingxi.controller;

import com.lingxi.entity.Agent;
import com.lingxi.entity.User;
import com.lingxi.service.AgentService;
import com.lingxi.util.ApiResponse;
import com.lingxi.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 智能体管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AgentController {

    private final AgentService agentService;
    private final JwtUtil jwtUtil;

    /**
     * 创建智能体
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAgent(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody Agent agent) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            // 验证智能体配置
            if (!agentService.validateAgentConfig(agent)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "智能体配置无效"));
            }
            
            Agent createdAgent = agentService.createAgent(agent, userId);
            
            log.info("Agent created: {} by user: {}", createdAgent.getName(), userId);
            return ResponseEntity.ok(Map.of(
                    "message", "智能体创建成功",
                    "agent", createdAgent
            ));
            
        } catch (Exception e) {
            log.error("Error creating agent", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 更新智能体
     */
    @PutMapping("/{agentId}")
    public ResponseEntity<Map<String, Object>> updateAgent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long agentId,
            @Valid @RequestBody Agent agent) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            Agent updatedAgent = agentService.updateAgent(agentId, agent, userId);
            
            log.info("Agent updated: {} by user: {}", agentId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "智能体更新成功",
                    "agent", updatedAgent
            ));
            
        } catch (Exception e) {
            log.error("Error updating agent: {}", agentId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 删除智能体
     */
    @DeleteMapping("/{agentId}")
    public ResponseEntity<Map<String, String>> deleteAgent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long agentId) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            agentService.deleteAgent(agentId, userId);
            
            log.info("Agent deleted: {} by user: {}", agentId, userId);
            return ResponseEntity.ok(Map.of("message", "智能体删除成功"));
            
        } catch (Exception e) {
            log.error("Error deleting agent: {}", agentId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取智能体详情
     */
    @GetMapping("/{agentId}")
    public ResponseEntity<Map<String, Object>> getAgent(@PathVariable Long agentId) {
        try {
            Optional<Agent> agent = agentService.getAgentById(agentId);
            
            if (agent.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of("agent", agent.get()));
            
        } catch (Exception e) {
            log.error("Error getting agent: {}", agentId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取所有智能体（默认返回活跃的智能体）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAgents() {
        try {
            List<Agent> agents = agentService.getActiveAgents();
            return ResponseEntity.ok(Map.of(
                    "agents", agents,
                    "total", agents.size()
            ));
            
        } catch (Exception e) {
            log.error("Error getting all agents", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取所有活跃的智能体
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveAgents() {
        try {
            List<Agent> agents = agentService.getActiveAgents();
            return ResponseEntity.ok(Map.of(
                    "agents", agents,
                    "total", agents.size()
            ));
            
        } catch (Exception e) {
            log.error("Error getting active agents", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取待审核的智能体（管理员）
     */
    /**
     * 获取已批准的智能体（管理员）
     */
    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<Page<Agent>>> getApprovedAgents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<Agent> agents = agentService.getApprovedAgents(pageable);
            return ResponseEntity.ok(ApiResponse.success(agents));

        } catch (Exception e) {
            log.error("Error getting approved agents", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<Agent>>> getPendingAgents(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            String role = extractRoleFromToken(authHeader);
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("权限不足"));
            }

            Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ?
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<Agent> agentPage = agentService.getPendingAgents(pageable);
            return ResponseEntity.ok(ApiResponse.success(agentPage));

        } catch (Exception e) {
            log.error("Error getting pending agents", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 审核智能体（管理员）
     */
    @PostMapping("/{agentId}/review")
    public ResponseEntity<Map<String, Object>> reviewAgent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long agentId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            String role = extractRoleFromToken(authHeader);
            
            if (userId == null || !"ADMIN".equals(role)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "权限不足"));
            }
            
            String statusStr = (String) request.get("status");
            String comment = (String) request.get("comment");
            
            Agent.AgentStatus status;
            try {
                status = Agent.AgentStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的审核状态"));
            }
            
            Agent reviewedAgent = agentService.reviewAgent(agentId, status, comment, userId);
            
            log.info("Agent reviewed: {} with status: {} by user: {}", agentId, status, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "智能体审核完成",
                    "agent", reviewedAgent
            ));
            
        } catch (Exception e) {
            log.error("Error reviewing agent: {}", agentId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 启用/禁用智能体
     */
    @PostMapping("/{agentId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleAgent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long agentId,
            @RequestBody Map<String, Boolean> request) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            Boolean enabled = request.get("enabled");
            if (enabled == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "enabled参数不能为空"));
            }
            
            Agent agent = agentService.toggleAgentStatus(agentId, enabled, userId);
            
            log.info("Agent toggled: {} to {} by user: {}", agentId, enabled, userId);
            return ResponseEntity.ok(Map.of(
                    "message", enabled ? "智能体已启用" : "智能体已禁用",
                    "agent", agent
            ));
            
        } catch (Exception e) {
            log.error("Error toggling agent: {}", agentId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取用户创建的智能体
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyAgents(
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            List<Agent> agents = agentService.getAgentsByCreator(userId);
            return ResponseEntity.ok(Map.of(
                    "agents", agents,
                    "total", agents.size()
            ));
            
        } catch (Exception e) {
            log.error("Error getting user agents", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 搜索智能体
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchAgents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Agent> agentPage = agentService.getAgentsByKeyword(keyword, pageable);
            
            return ResponseEntity.ok(Map.of(
                    "agents", agentPage.getContent(),
                    "total", agentPage.getTotalElements(),
                    "totalPages", agentPage.getTotalPages(),
                    "currentPage", agentPage.getNumber(),
                    "size", agentPage.getSize()
            ));
            
        } catch (Exception e) {
            log.error("Error searching agents", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }



    /**
     * 获取智能体统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAgentStats(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String role = extractRoleFromToken(authHeader);
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "权限不足"));
            }
            
            List<Object[]> statusStats = agentService.getAgentStatsByStatus();
            List<Object[]> typeStats = agentService.getAgentStatsByType();
            
            return ResponseEntity.ok(Map.of(
                    "statusStats", statusStats,
                    "typeStats", typeStats
            ));
            
        } catch (Exception e) {
            log.error("Error getting agent stats", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 检查智能体健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkAgentHealth(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String role = extractRoleFromToken(authHeader);
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "权限不足"));
            }
            
            List<Agent> unhealthyAgents = agentService.checkAgentHealth();
            
            return ResponseEntity.ok(Map.of(
                    "unhealthyAgents", unhealthyAgents,
                    "total", unhealthyAgents.size()
            ));
            
        } catch (Exception e) {
            log.error("Error checking agent health", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 根据能力搜索智能体
     */
    @GetMapping("/by-capability")
    public ResponseEntity<Map<String, Object>> getAgentsByCapability(
            @RequestParam String capability) {
        try {
            List<Agent> agents = agentService.getAgentsByCapability(capability);
            
            return ResponseEntity.ok(Map.of(
                    "agents", agents,
                    "total", agents.size()
            ));
            
        } catch (Exception e) {
            log.error("Error getting agents by capability", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 根据名称获取智能体
     */
    @GetMapping("/by-name/{name}")
    public ResponseEntity<Map<String, Object>> getAgentByName(@PathVariable String name) {
        try {
            Optional<Agent> agent = agentService.getAgentByName(name);
            
            if (agent.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of("agent", agent.get()));
            
        } catch (Exception e) {
            log.error("Error getting agent by name: {}", name, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 从token中提取用户ID
     */
    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 从token中提取用户角色
     */
    private String extractRoleFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        
        return jwtUtil.getRoleFromToken(token);
    }
}