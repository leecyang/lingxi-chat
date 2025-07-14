package com.lingxi.controller;

import com.lingxi.entity.DeveloperRequest;
import com.lingxi.service.DeveloperRequestService;
import com.lingxi.util.JwtUtil;
import com.lingxi.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * 开发者申请控制器
 */
@Slf4j
@RestController
@RequestMapping("/developer-requests")
@RequiredArgsConstructor
public class DeveloperRequestController {

    private final DeveloperRequestService developerRequestService;
    private final JwtUtil jwtUtil;

    /**
     * 创建开发者申请
     */
    @PostMapping
    public ResponseEntity<DeveloperRequest> createDeveloperRequest(
            @RequestBody CreateDeveloperRequestDto dto) {
        try {
            DeveloperRequest request = new DeveloperRequest();
            request.setReason(dto.getReason());
            request.setSkills(dto.getSkillDescription());
            request.setExperience(dto.getProjectExperience());
            request.setContactInfo(dto.getContactInfo());
            
            request = developerRequestService.createRequest(request, dto.getUserId());
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            log.error("Error creating developer request", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 审核开发者申请
     */
    @PostMapping("/{requestId}/review")
    public ResponseEntity<DeveloperRequest> reviewDeveloperRequest(
            @PathVariable Long requestId,
            @RequestBody ReviewDeveloperRequestDto dto) {
        try {
            DeveloperRequest request = developerRequestService.reviewRequest(
                    requestId,
                    dto.getStatus(),
                    dto.getReviewNotes(),
                    dto.getReviewerId()
            );
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            log.error("Error reviewing developer request: {}", requestId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 取消开发者申请
     */
    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<DeveloperRequest> cancelDeveloperRequest(
            @PathVariable Long requestId,
            @RequestParam Long userId) {
        try {
            DeveloperRequest request = developerRequestService.cancelRequest(requestId, userId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            log.error("Error canceling developer request: {}", requestId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取开发者申请详情
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<DeveloperRequest> getDeveloperRequest(@PathVariable Long requestId) {
        Optional<DeveloperRequest> request = developerRequestService.getRequestById(requestId);
        return request.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取待审核的申请
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            // 验证token并检查是否为管理员
            String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            String userRole = jwtUtil.getRoleFromToken(actualToken);
            
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Access denied. Admin role required."));
            }
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<DeveloperRequest> requests = developerRequestService.getPendingRequests(pageable);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            log.error("Error getting pending requests", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Internal server error: " + e.getMessage()));
        }
    }

    /**
     * 获取已批准的申请
     */
    @GetMapping("/approved")
    public ResponseEntity<Page<DeveloperRequest>> getApprovedRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reviewedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DeveloperRequest> requests = developerRequestService.getApprovedRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * 获取已拒绝的申请
     */
    @GetMapping("/rejected")
    public ResponseEntity<Page<DeveloperRequest>> getRejectedRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reviewedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DeveloperRequest> requests = developerRequestService.getRejectedRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * 根据状态获取申请
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<DeveloperRequest>> getRequestsByStatus(
            @PathVariable DeveloperRequest.RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DeveloperRequest> requests = developerRequestService.getRequestsByStatus(status, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * 获取用户的申请记录
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<DeveloperRequest>> getUserRequests(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DeveloperRequest> requests = developerRequestService.getUserRequests(userId, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * 获取审核人的审核记录
     */
    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<Page<DeveloperRequest>> getReviewerRequests(
            @PathVariable Long reviewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reviewedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DeveloperRequest> requests = developerRequestService.getReviewerRequests(reviewerId, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * 搜索申请
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DeveloperRequest>> searchRequests(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DeveloperRequest> requests = developerRequestService.searchRequests(keyword, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * 获取最近的申请
     */
    @GetMapping("/recent")
    public ResponseEntity<Page<DeveloperRequest>> getRecentRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DeveloperRequest> requests = developerRequestService.getRecentRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * 获取指定时间范围内的申请
     */
    @GetMapping("/date-range")
    public ResponseEntity<Page<DeveloperRequest>> getRequestsByDateRange(
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
            
            Page<DeveloperRequest> requests = developerRequestService.getRequestsByDateRange(start, end, pageable);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            log.error("Error parsing date range", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Object> getStatistics() {
        Object statistics = developerRequestService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取指定时间范围内的统计信息
     */
    @GetMapping("/statistics/date-range")
    public ResponseEntity<Object> getStatisticsByDateRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            
            Object statistics = developerRequestService.getStatisticsByDateRange(start, end);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error parsing date range for statistics", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 创建开发者申请DTO
     */
    public static class CreateDeveloperRequestDto {
        private Long userId;
        private String reason;
        private String skillDescription;
        private String projectExperience;
        private String contactInfo;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getSkillDescription() { return skillDescription; }
        public void setSkillDescription(String skillDescription) { this.skillDescription = skillDescription; }
        
        public String getProjectExperience() { return projectExperience; }
        public void setProjectExperience(String projectExperience) { this.projectExperience = projectExperience; }
        
        public String getContactInfo() { return contactInfo; }
        public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    }

    /**
     * 审核开发者申请DTO
     */
    public static class ReviewDeveloperRequestDto {
        private DeveloperRequest.RequestStatus status;
        private String reviewNotes;
        private Long reviewerId;

        // Getters and Setters
        public DeveloperRequest.RequestStatus getStatus() { return status; }
        public void setStatus(DeveloperRequest.RequestStatus status) { this.status = status; }
        
        public String getReviewNotes() { return reviewNotes; }
        public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
        
        public Long getReviewerId() { return reviewerId; }
        public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    }
}