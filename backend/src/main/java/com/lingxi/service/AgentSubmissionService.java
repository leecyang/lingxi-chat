package com.lingxi.service;

import com.lingxi.entity.Agent;
import com.lingxi.entity.AgentSubmission;
import com.lingxi.entity.User;
import com.lingxi.repository.AgentSubmissionRepository;
import com.lingxi.repository.UserRepository;
import com.lingxi.repository.AgentRepository;
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
 * 智能体提交申请服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AgentSubmissionService {

    private final AgentSubmissionRepository agentSubmissionRepository;
    private final AgentService agentService;
    private final UserRepository userRepository;
    private final AgentRepository agentRepository;

    /**
     * 提交智能体申请
     */
    public AgentSubmission submitAgent(String name, String description, String apiUrl, 
                                     String appId, String apiKey, String token, String category, Long submitterId) {
        User submitter = userRepository.findById(submitterId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查用户权限
        if (!canSubmitAgent(submitter)) {
            throw new RuntimeException("您没有权限提交智能体申请");
        }

        // 检查是否已存在同名申请
        if (agentSubmissionRepository.existsBySubmitterAndName(submitter, name)) {
            throw new RuntimeException("您已提交过同名的智能体申请");
        }

        // 检查是否存在同名的待审核申请
        if (agentSubmissionRepository.existsByNameAndStatus(name, AgentSubmission.SubmissionStatus.PENDING)) {
            throw new RuntimeException("已存在同名的待审核申请");
        }

        AgentSubmission submission = new AgentSubmission();
        submission.setName(name);
        submission.setDescription(description);
        submission.setApiUrl(apiUrl);
        submission.setAppId(appId);
        submission.setApiKey(apiKey);
        submission.setToken(token);
        submission.setCategory(category);
        submission.setSubmitter(submitter);
        submission.setSubmitterRole(submitter.getRole());
        submission.setStatus(AgentSubmission.SubmissionStatus.PENDING);

        AgentSubmission saved = agentSubmissionRepository.save(submission);
        log.info("用户 {} 提交了智能体申请: {}", submitter.getUsername(), name);
        return saved;
    }

    /**
     * 检查用户是否可以提交智能体申请
     */
    public boolean canSubmitAgent(User user) {
        return user.getRole() == User.UserRole.ADMIN || 
               user.getRole() == User.UserRole.TEACHER ||
               (user.getRole() == User.UserRole.DEVELOPER && user.getStatus() == User.UserStatus.ACTIVE);
    }

    /**
     * 审核智能体申请
     */
    public AgentSubmission reviewSubmission(Long submissionId, Long reviewerId, 
                                          AgentSubmission.SubmissionStatus status, String notes) {
        AgentSubmission submission = agentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("申请不存在"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("审核者不存在"));

        if (reviewer.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("只有管理员可以审核申请");
        }

        if (!submission.isPending()) {
            throw new RuntimeException("该申请已被审核");
        }

        if (status == AgentSubmission.SubmissionStatus.APPROVED) {
            // 创建智能体
            Agent agent = createAgentFromSubmission(submission);
            submission.approve(reviewer, notes, agent.getId());
            log.info("管理员 {} 批准了智能体申请: {}", reviewer.getUsername(), submission.getName());
        } else if (status == AgentSubmission.SubmissionStatus.REJECTED) {
            submission.reject(reviewer, notes);
            log.info("管理员 {} 拒绝了智能体申请: {}", reviewer.getUsername(), submission.getName());
        } else if (status == AgentSubmission.SubmissionStatus.TESTING) {
            submission.setTesting(reviewer, notes);
            log.info("管理员 {} 将智能体申请设为测试中: {}", reviewer.getUsername(), submission.getName());
        }

        return agentSubmissionRepository.save(submission);
    }

    /**
     * 从申请创建智能体
     */
    private Agent createAgentFromSubmission(AgentSubmission submission) {
        Agent agent = new Agent();
        agent.setName(submission.getName());
        agent.setDisplayName(submission.getName());
        agent.setDescription(submission.getDescription());
        agent.setModelId("custom-" + submission.getName().toLowerCase().replaceAll("\\s+", "-"));
        agent.setEndpoint(submission.getApiUrl());
        agent.setAppId(submission.getAppId());
        agent.setApiKey(submission.getApiKey());
        agent.setToken(submission.getToken());
        agent.setStatus(Agent.AgentStatus.APPROVED);
        agent.setType(Agent.AgentType.CUSTOM);
        
        // 设置智能体分类
        try {
            Agent.AgentCategory category = Agent.AgentCategory.valueOf(submission.getCategory());
            agent.setCategory(category);
        } catch (IllegalArgumentException e) {
            // 如果分类不匹配，默认设置为生活护航
            agent.setCategory(Agent.AgentCategory.LIFE_SUPPORT);
        }
        
        agent.setCreator(submission.getSubmitter());
        agent.setApprover(submission.getReviewer());
        agent.setApprovalTime(LocalDateTime.now());
        agent.setApprovalNotes(submission.getReviewNotes());
        agent.setEnabled(true);
        
        return agentRepository.save(agent);
    }

    /**
     * 获取用户的申请列表
     */
    @Transactional(readOnly = true)
    public List<AgentSubmission> getUserSubmissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return agentSubmissionRepository.findBySubmitter(user);
    }

    /**
     * 获取待审核的申请列表（分页）
     */
    @Transactional(readOnly = true)
    public Page<AgentSubmission> getPendingSubmissions(Pageable pageable) {
        return agentSubmissionRepository.findByStatus(AgentSubmission.SubmissionStatus.PENDING, pageable);
    }

    /**
     * 获取所有申请列表（分页）
     */
    @Transactional(readOnly = true)
    public Page<AgentSubmission> getAllSubmissions(Pageable pageable) {
        return agentSubmissionRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * 根据ID获取申请
     */
    @Transactional(readOnly = true)
    public Optional<AgentSubmission> findById(Long id) {
        return agentSubmissionRepository.findById(id);
    }

    /**
     * 根据状态获取申请列表
     */
    @Transactional(readOnly = true)
    public List<AgentSubmission> getSubmissionsByStatus(AgentSubmission.SubmissionStatus status) {
        return agentSubmissionRepository.findByStatus(status);
    }

    /**
     * 统计用户的申请数量
     */
    @Transactional(readOnly = true)
    public long countUserSubmissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return agentSubmissionRepository.countBySubmitter(user);
    }

    /**
     * 统计用户待审核的申请数量
     */
    @Transactional(readOnly = true)
    public long countUserPendingSubmissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return agentSubmissionRepository.countBySubmitterAndStatus(user, AgentSubmission.SubmissionStatus.PENDING);
    }

    /**
     * 删除申请（仅限提交者或管理员）
     */
    public void deleteSubmission(Long submissionId, Long userId) {
        AgentSubmission submission = agentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("申请不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!submission.getSubmitter().getId().equals(userId) && user.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("您没有权限删除此申请");
        }

        if (submission.isApproved()) {
            throw new RuntimeException("已通过的申请不能删除");
        }

        agentSubmissionRepository.delete(submission);
        log.info("用户 {} 删除了智能体申请: {}", user.getUsername(), submission.getName());
    }
}