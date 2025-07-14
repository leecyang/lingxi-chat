package com.lingxi.controller;

import com.lingxi.entity.ConversationLog;
import com.lingxi.service.ConversationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 对话日志控制器
 */
@Slf4j
@RestController
@RequestMapping("/conversation-logs")
@RequiredArgsConstructor
public class ConversationLogController {

    private final ConversationLogService conversationLogService;

    /**
     * 创建对话日志
     */
    @PostMapping
    public ResponseEntity<ConversationLog> createConversationLog(
            @RequestBody CreateConversationLogDto dto) {
        try {
            ConversationLog log = conversationLogService.createConversationLog(
                    dto.getSessionId(),
                    dto.getUserId(),
                    dto.getAgentId()
            );
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            log.error("Error creating conversation log", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 完成对话日志
     */
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<ConversationLog> completeConversationLog(
            @PathVariable String sessionId,
            @RequestBody(required = false) CompleteConversationLogDto dto) {
        try {
            String summary = dto != null ? dto.getSummary() : null;
            ConversationLog log = conversationLogService.completeConversationLog(sessionId, summary);
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            log.error("Error completing conversation log: {}", sessionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 标记对话日志
     */
    @PostMapping("/{logId}/flag")
    public ResponseEntity<ConversationLog> flagConversationLog(
            @PathVariable Long logId,
            @RequestBody FlagConversationLogDto dto) {
        try {
            ConversationLog log = conversationLogService.flagConversationLog(
                    logId,
                    dto.getReason(),
                    dto.getFlaggedById()
            );
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            log.error("Error flagging conversation log: {}", logId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 取消标记对话日志
     */
    @PostMapping("/{logId}/unflag")
    public ResponseEntity<ConversationLog> unflagConversationLog(
            @PathVariable Long logId,
            @RequestParam Long userId) {
        try {
            ConversationLog log = conversationLogService.unflagConversationLog(logId, userId);
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            log.error("Error unflagging conversation log: {}", logId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 设置内容评级
     */
    @PostMapping("/{logId}/rating")
    public ResponseEntity<ConversationLog> setContentRating(
            @PathVariable Long logId,
            @RequestBody SetContentRatingDto dto) {
        try {
            ConversationLog log = conversationLogService.setContentRating(
                    logId,
                    dto.getRating(),
                    dto.getUserId()
            );
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            log.error("Error setting content rating for conversation log: {}", logId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 终止对话日志
     */
    @PostMapping("/{logId}/terminate")
    public ResponseEntity<ConversationLog> terminateConversationLog(
            @PathVariable Long logId,
            @RequestBody TerminateConversationLogDto dto) {
        try {
            ConversationLog log = conversationLogService.terminateConversationLog(
                    logId,
                    dto.getReason(),
                    dto.getUserId()
            );
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            log.error("Error terminating conversation log: {}", logId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 归档对话日志
     */
    @PostMapping("/{logId}/archive")
    public ResponseEntity<ConversationLog> archiveConversationLog(
            @PathVariable Long logId,
            @RequestParam Long userId) {
        try {
            ConversationLog log = conversationLogService.archiveConversationLog(logId, userId);
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            log.error("Error archiving conversation log: {}", logId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取对话日志详情
     */
    @GetMapping("/{logId}")
    public ResponseEntity<ConversationLog> getConversationLog(@PathVariable Long logId) {
        Optional<ConversationLog> log = conversationLogService.getConversationLogById(logId);
        return log.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据会话ID获取对话日志
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ConversationLog> getConversationLogBySessionId(@PathVariable String sessionId) {
        Optional<ConversationLog> log = conversationLogService.getConversationLogBySessionId(sessionId);
        return log.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取需要审核的对话日志
     */
    @GetMapping("/review")
    public ResponseEntity<Page<ConversationLog>> getLogsNeedingReview(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastActivityAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ConversationLog> logs = conversationLogService.getLogsNeedingReview(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取被标记的对话日志
     */
    @GetMapping("/flagged")
    public ResponseEntity<Page<ConversationLog>> getFlaggedLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "flaggedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ConversationLog> logs = conversationLogService.getFlaggedLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取活跃的对话日志
     */
    @GetMapping("/active")
    public ResponseEntity<Page<ConversationLog>> getActiveLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastActivityAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ConversationLog> logs = conversationLogService.getActiveLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据状态获取对话日志
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ConversationLog>> getLogsByStatus(
            @PathVariable ConversationLog.LogStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ConversationLog> logs = conversationLogService.getLogsByStatus(status, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据内容评级获取对话日志
     */
    @GetMapping("/rating/{rating}")
    public ResponseEntity<Page<ConversationLog>> getLogsByContentRating(
            @PathVariable ConversationLog.ContentRating rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ConversationLog> logs = conversationLogService.getLogsByContentRating(rating, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 搜索对话日志
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ConversationLog>> searchLogs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ConversationLog> logs = conversationLogService.searchLogs(keyword, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取最近的对话日志
     */
    @GetMapping("/recent")
    public ResponseEntity<Page<ConversationLog>> getRecentLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastActivityAt").descending());
        Page<ConversationLog> logs = conversationLogService.getRecentLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取用户的对话日志
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConversationLog>> getUserLogs(@PathVariable Long userId) {
        List<ConversationLog> logs = conversationLogService.getUserLogs(userId);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取智能体的对话日志
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ConversationLog>> getAgentLogs(@PathVariable Long agentId) {
        List<ConversationLog> logs = conversationLogService.getAgentLogs(agentId);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取指定时间范围内的对话日志
     */
    @GetMapping("/date-range")
    public ResponseEntity<Page<ConversationLog>> getLogsByDateRange(
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ConversationLog> logs = conversationLogService.getLogsByDateRange(start, end, pageable);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error parsing date range", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = conversationLogService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取指定时间范围内的统计信息
     */
    @GetMapping("/statistics/date-range")
    public ResponseEntity<Map<String, Object>> getStatisticsByDateRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            
            Map<String, Object> statistics = conversationLogService.getStatisticsByDateRange(start, end);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error parsing date range for statistics", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 清理长时间无活动的对话日志
     */
    @PostMapping("/cleanup/inactive")
    public ResponseEntity<String> cleanupInactiveLogs(
            @RequestParam(defaultValue = "24") int hours) {
        try {
            conversationLogService.cleanupInactiveLogs(hours);
            return ResponseEntity.ok("清理完成");
        } catch (Exception e) {
            log.error("Error cleaning up inactive logs", e);
            return ResponseEntity.badRequest().body("清理失败: " + e.getMessage());
        }
    }

    /**
     * 清理旧的已归档对话日志
     */
    @PostMapping("/cleanup/archived")
    public ResponseEntity<String> cleanupOldArchivedLogs(
            @RequestParam(defaultValue = "90") int days) {
        try {
            conversationLogService.cleanupOldArchivedLogs(days);
            return ResponseEntity.ok("清理完成");
        } catch (Exception e) {
            log.error("Error cleaning up old archived logs", e);
            return ResponseEntity.badRequest().body("清理失败: " + e.getMessage());
        }
    }

    // DTO Classes
    
    /**
     * 创建对话日志DTO
     */
    public static class CreateConversationLogDto {
        private String sessionId;
        private Long userId;
        private Long agentId;

        // Getters and Setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getAgentId() { return agentId; }
        public void setAgentId(Long agentId) { this.agentId = agentId; }
    }

    /**
     * 完成对话日志DTO
     */
    public static class CompleteConversationLogDto {
        private String summary;

        // Getters and Setters
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    /**
     * 标记对话日志DTO
     */
    public static class FlagConversationLogDto {
        private String reason;
        private Long flaggedById;

        // Getters and Setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public Long getFlaggedById() { return flaggedById; }
        public void setFlaggedById(Long flaggedById) { this.flaggedById = flaggedById; }
    }

    /**
     * 设置内容评级DTO
     */
    public static class SetContentRatingDto {
        private ConversationLog.ContentRating rating;
        private Long userId;

        // Getters and Setters
        public ConversationLog.ContentRating getRating() { return rating; }
        public void setRating(ConversationLog.ContentRating rating) { this.rating = rating; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    /**
     * 终止对话日志DTO
     */
    public static class TerminateConversationLogDto {
        private String reason;
        private Long userId;

        // Getters and Setters
        public String reason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getReason() { return reason; }
    }
}