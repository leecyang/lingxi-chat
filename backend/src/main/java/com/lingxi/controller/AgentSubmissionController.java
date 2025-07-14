package com.lingxi.controller;

import com.lingxi.entity.AgentSubmission;
import com.lingxi.service.AgentSubmissionService;
import com.lingxi.util.ApiResponse;
import com.lingxi.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 智能体提交申请控制器
 */
@RestController
@RequestMapping("/agent-submissions")

@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AgentSubmissionController {

    private final AgentSubmissionService agentSubmissionService;
    private final JwtUtil jwtUtil;

    /**
     * 提交智能体申请
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<AgentSubmission>> submitAgent(@Valid @RequestBody SubmitAgentRequest request) {
        try {
            AgentSubmission submission = agentSubmissionService.submitAgent(
                    request.getName(),
                    request.getDescription(),
                    request.getApiUrl(),
                    request.getAppId(),
                    request.getApiKey(),
                    request.getToken(),
                    request.getCategory(),
                    request.getSubmitterId()
            );
            return ResponseEntity.ok(ApiResponse.success(submission, "智能体申请提交成功，等待管理员审核"));
        } catch (Exception e) {
            log.error("提交智能体申请失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 审核智能体申请
     */
    @PostMapping("/review/{submissionId}")
    public ResponseEntity<ApiResponse<AgentSubmission>> reviewSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody ReviewSubmissionRequest request) {
        try {
            AgentSubmission submission = agentSubmissionService.reviewSubmission(
                    submissionId,
                    request.getReviewerId(),
                    request.getStatus(),
                    request.getNotes()
            );
            return ResponseEntity.ok(ApiResponse.success(submission, "审核完成"));
        } catch (Exception e) {
            log.error("审核智能体申请失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取用户的申请列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AgentSubmission>>> getUserSubmissions(@PathVariable Long userId) {
        try {
            List<AgentSubmission> submissions = agentSubmissionService.getUserSubmissions(userId);
            return ResponseEntity.ok(ApiResponse.success(submissions));
        } catch (Exception e) {
            log.error("获取用户申请列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取待审核的申请列表（分页）
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<AgentSubmission>>> getPendingSubmissions(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String role = jwtUtil.getRoleFromToken(authHeader.substring(7));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body(ApiResponse.error("权限不足"));
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<AgentSubmission> submissions = agentSubmissionService.getPendingSubmissions(pageable);
            return ResponseEntity.ok(ApiResponse.success(submissions));
        } catch (Exception e) {
            log.error("获取待审核申请列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取所有申请列表（分页）
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<AgentSubmission>>> getAllSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AgentSubmission> submissions = agentSubmissionService.getAllSubmissions(pageable);
            return ResponseEntity.ok(ApiResponse.success(submissions));
        } catch (Exception e) {
            log.error("获取申请列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据ID获取申请详情
     */
    @GetMapping("/{submissionId}")
    public ResponseEntity<ApiResponse<AgentSubmission>> getSubmission(@PathVariable Long submissionId) {
        try {
            AgentSubmission submission = agentSubmissionService.findById(submissionId)
                    .orElseThrow(() -> new RuntimeException("申请不存在"));
            return ResponseEntity.ok(ApiResponse.success(submission));
        } catch (Exception e) {
            log.error("获取申请详情失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除申请
     */
    @DeleteMapping("/{submissionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubmission(
            @PathVariable Long submissionId,
            @RequestParam Long userId) {
        try {
            agentSubmissionService.deleteSubmission(submissionId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "申请删除成功"));
        } catch (Exception e) {
            log.error("删除申请失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 统计用户申请数量
     */
    @GetMapping("/count/user/{userId}")
    public ResponseEntity<ApiResponse<Long>> countUserSubmissions(@PathVariable Long userId) {
        try {
            long count = agentSubmissionService.countUserSubmissions(userId);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("统计用户申请数量失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 统计用户待审核申请数量
     */
    @GetMapping("/count/pending/user/{userId}")
    public ResponseEntity<ApiResponse<Long>> countUserPendingSubmissions(@PathVariable Long userId) {
        try {
            long count = agentSubmissionService.countUserPendingSubmissions(userId);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("统计用户待审核申请数量失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 提交智能体申请请求DTO
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubmitAgentRequest {
        private String name;
        private String description;
        private String apiUrl;
        private String appId;
        private String apiKey;
        private String token;
        private String category;
        private Long submitterId;
        private String submitterRole;
        private String status;
    }

    /**
     * 审核申请请求DTO
     */
    @Data
    public static class ReviewSubmissionRequest {
        private Long reviewerId;
        private AgentSubmission.SubmissionStatus status;
        private String notes;
    }
}