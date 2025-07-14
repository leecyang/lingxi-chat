package com.lingxi.service;

import com.lingxi.entity.DeveloperRequest;
import com.lingxi.entity.User;
import com.lingxi.repository.DeveloperRequestRepository;
import com.lingxi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 开发者申请服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeveloperRequestService {

    private final DeveloperRequestRepository developerRequestRepository;
    private final UserRepository userRepository;

    /**
     * 创建开发者申请
     */
    @Transactional
    public DeveloperRequest createRequest(DeveloperRequest request, Long userId) {
        log.info("Creating developer request for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查用户是否已经是开发者
        if (user.getRole().equals(User.UserRole.DEVELOPER) || user.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("用户已经具有开发者权限");
        }
        
        // 检查是否有待审核的申请
        if (developerRequestRepository.existsByUserAndStatus(user, DeveloperRequest.RequestStatus.PENDING)) {
            throw new RuntimeException("您已有待审核的申请，请等待审核结果");
        }
        
        request.setUser(user);
        request.setStatus(DeveloperRequest.RequestStatus.PENDING);
        
        DeveloperRequest savedRequest = developerRequestRepository.save(request);
        log.info("Developer request created with ID: {}", savedRequest.getId());
        
        return savedRequest;
    }

    /**
     * 审核开发者申请
     */
    @Transactional
    public DeveloperRequest reviewRequest(Long requestId, DeveloperRequest.RequestStatus status, 
                                        String reviewNotes, Long reviewerId) {
        log.info("Reviewing developer request: {} with status: {} by reviewer: {}", 
                requestId, status, reviewerId);
        
        DeveloperRequest request = developerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("申请记录不存在"));
        
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("审核者不存在"));
        
        // 权限检查：只有管理员可以审核
        if (!reviewer.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("只有管理员可以审核开发者申请");
        }
        
        // 检查申请状态
        if (!request.isPending()) {
            throw new RuntimeException("申请已被处理，无法重复审核");
        }
        
        if (status == DeveloperRequest.RequestStatus.APPROVED) {
            request.approve(reviewer, reviewNotes);
            // 更新用户角色为开发者
            User applicant = request.getUser();
            applicant.setRole(User.UserRole.DEVELOPER);
            userRepository.save(applicant);
            log.info("User {} promoted to developer", applicant.getUsername());
        } else if (status == DeveloperRequest.RequestStatus.REJECTED) {
            request.reject(reviewer, reviewNotes);
        } else {
            throw new RuntimeException("无效的审核状态");
        }
        
        DeveloperRequest savedRequest = developerRequestRepository.save(request);
        log.info("Developer request {} reviewed with status: {}", requestId, status);
        
        return savedRequest;
    }

    /**
     * 取消申请
     */
    @Transactional
    public DeveloperRequest cancelRequest(Long requestId, Long userId) {
        log.info("Cancelling developer request: {} by user: {}", requestId, userId);
        
        DeveloperRequest request = developerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("申请记录不存在"));
        
        // 权限检查：只有申请者本人或管理员可以取消
        if (!request.getUser().getId().equals(userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            if (!user.getRole().equals(User.UserRole.ADMIN)) {
                throw new RuntimeException("只有申请者本人或管理员可以取消申请");
            }
        }
        
        // 检查申请状态
        if (!request.isPending()) {
            throw new RuntimeException("只能取消待审核的申请");
        }
        
        request.cancel();
        DeveloperRequest savedRequest = developerRequestRepository.save(request);
        log.info("Developer request {} cancelled", requestId);
        
        return savedRequest;
    }

    /**
     * 获取申请详情
     */
    public Optional<DeveloperRequest> getRequestById(Long requestId) {
        return developerRequestRepository.findById(requestId);
    }

    /**
     * 获取用户的申请记录
     */
    public List<DeveloperRequest> getUserRequests(Long userId) {
        return developerRequestRepository.findByUserId(userId);
    }

    /**
     * 分页获取用户的申请记录
     */
    public Page<DeveloperRequest> getUserRequests(Long userId, Pageable pageable) {
        return developerRequestRepository.findByUserId(userId, pageable);
    }

    /**
     * 获取用户的最新申请
     */
    public Optional<DeveloperRequest> getUserLatestRequest(Long userId) {
        return developerRequestRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 获取待审核的申请列表
     */
    public List<DeveloperRequest> getPendingRequests() {
        return developerRequestRepository.findPendingRequests();
    }

    /**
     * 分页获取待审核的申请列表
     */
    public Page<DeveloperRequest> getPendingRequests(Pageable pageable) {
        return developerRequestRepository.findPendingRequests(pageable);
    }

    /**
     * 根据状态获取申请列表
     */
    public List<DeveloperRequest> getRequestsByStatus(DeveloperRequest.RequestStatus status) {
        return developerRequestRepository.findByStatus(status);
    }

    /**
     * 分页根据状态获取申请列表
     */
    public Page<DeveloperRequest> getRequestsByStatus(DeveloperRequest.RequestStatus status, Pageable pageable) {
        return developerRequestRepository.findByStatus(status, pageable);
    }

    /**
     * 分页获取已批准的申请列表
     */
    public Page<DeveloperRequest> getApprovedRequests(Pageable pageable) {
        return developerRequestRepository.findByStatus(DeveloperRequest.RequestStatus.APPROVED, pageable);
    }

    /**
     * 分页获取已拒绝的申请列表
     */
    public Page<DeveloperRequest> getRejectedRequests(Pageable pageable) {
        return developerRequestRepository.findByStatus(DeveloperRequest.RequestStatus.REJECTED, pageable);
    }

    /**
     * 提交开发者申请（新方法，适配前端）
     */
    @Transactional
    public DeveloperRequest submitRequest(Long userId, String reason, String skills, 
                                        String experience, String contactInfo) {
        log.info("Submitting developer request for user: {}", userId);
        
        DeveloperRequest request = new DeveloperRequest();
        request.setReason(reason);
        request.setSkills(skills);
        request.setExperience(experience);
        request.setContactInfo(contactInfo);
        
        return createRequest(request, userId);
    }

    /**
     * 审核开发者申请（新方法，适配前端）
     */
    @Transactional
    public DeveloperRequest reviewRequest(Long requestId, Long reviewerId, 
                                        boolean approved, String notes) {
        DeveloperRequest.RequestStatus status = approved ? 
            DeveloperRequest.RequestStatus.APPROVED : DeveloperRequest.RequestStatus.REJECTED;
        return reviewRequest(requestId, status, notes, reviewerId);
    }

    /**
     * 检查用户是否可以申请开发者
     */
    public boolean canApplyDeveloper(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查用户角色
        if (user.getRole() != User.UserRole.USER) {
            return false;
        }
        
        // 检查是否有待审核的申请
        return !developerRequestRepository.existsByUserAndStatus(user, DeveloperRequest.RequestStatus.PENDING);
    }

    /**
     * 获取所有申请列表（分页）
     */
    public Page<DeveloperRequest> getAllRequests(Pageable pageable) {
        return developerRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * 根据ID查找申请
     */
    public Optional<DeveloperRequest> findById(Long id) {
        return developerRequestRepository.findById(id);
    }

    /**
     * 统计用户申请数量
     */
    public long countUserRequests(Long userId) {
        return developerRequestRepository.countByUserId(userId);
    }

    /**
     * 搜索申请记录
     */
    public Page<DeveloperRequest> searchRequests(String keyword, Pageable pageable) {
        return developerRequestRepository.searchRequests(keyword, pageable);
    }

    /**
     * 获取最近的申请记录
     */
    public Page<DeveloperRequest> getRecentRequests(Pageable pageable) {
        return developerRequestRepository.findRecentRequests(pageable);
    }

    /**
     * 获取超时未处理的申请
     */
    public List<DeveloperRequest> getOverdueRequests(int days) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        return developerRequestRepository.findOverdueRequests(cutoffTime);
    }

    /**
     * 统计各状态的申请数量
     */
    public List<Object[]> getStatusStatistics() {
        return developerRequestRepository.countByStatus();
    }

    /**
     * 统计用户的申请次数
     */
    public long getUserRequestCount(Long userId) {
        return developerRequestRepository.countByUserId(userId);
    }

    /**
     * 检查用户是否有待审核的申请
     */
    public boolean hasUserPendingRequest(Long userId) {
        return developerRequestRepository.existsByUserIdAndStatus(userId, DeveloperRequest.RequestStatus.PENDING);
    }

    /**
     * 批量审核申请
     */
    @Transactional
    public void batchReviewRequests(List<Long> requestIds, DeveloperRequest.RequestStatus status, 
                                  String reviewNotes, Long reviewerId) {
        log.info("Batch reviewing {} requests with status: {} by reviewer: {}", 
                requestIds.size(), status, reviewerId);
        
        for (Long requestId : requestIds) {
            try {
                reviewRequest(requestId, status, reviewNotes, reviewerId);
            } catch (Exception e) {
                log.error("Failed to review request {}: {}", requestId, e.getMessage());
            }
        }
    }

    /**
     * 清理旧的已处理申请记录
     */
    @Transactional
    public void cleanupOldProcessedRequests(int days) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        developerRequestRepository.deleteOldProcessedRequests(cutoffTime);
        log.info("Cleaned up old processed requests before: {}", cutoffTime);
    }

    /**
     * 获取审核者的申请记录
     */
    public Page<DeveloperRequest> getReviewerRequests(Long reviewerId, Pageable pageable) {
        return developerRequestRepository.findByReviewerId(reviewerId, pageable);
    }

    /**
     * 根据日期范围获取申请记录
     */
    public Page<DeveloperRequest> getRequestsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return developerRequestRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    /**
     * 获取统计信息
     */
    public Object getStatistics() {
        return new Object() {
            public final long totalRequests = developerRequestRepository.count();
            public final long pendingRequests = developerRequestRepository.countByStatus(DeveloperRequest.RequestStatus.PENDING);
            public final long approvedRequests = developerRequestRepository.countByStatus(DeveloperRequest.RequestStatus.APPROVED);
            public final long rejectedRequests = developerRequestRepository.countByStatus(DeveloperRequest.RequestStatus.REJECTED);
            public final long cancelledRequests = developerRequestRepository.countByStatus(DeveloperRequest.RequestStatus.CANCELLED);
        };
    }

    /**
     * 根据日期范围获取统计信息
     */
    public Object getStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return new Object() {
            public final long totalRequests = developerRequestRepository.countByCreatedAtBetween(startDate, endDate);
            public final long pendingRequests = developerRequestRepository.countByStatusAndCreatedAtBetween(DeveloperRequest.RequestStatus.PENDING, startDate, endDate);
            public final long approvedRequests = developerRequestRepository.countByStatusAndCreatedAtBetween(DeveloperRequest.RequestStatus.APPROVED, startDate, endDate);
            public final long rejectedRequests = developerRequestRepository.countByStatusAndCreatedAtBetween(DeveloperRequest.RequestStatus.REJECTED, startDate, endDate);
            public final long cancelledRequests = developerRequestRepository.countByStatusAndCreatedAtBetween(DeveloperRequest.RequestStatus.CANCELLED, startDate, endDate);
        };
    }
}