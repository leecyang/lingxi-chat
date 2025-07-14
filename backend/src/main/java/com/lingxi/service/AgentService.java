package com.lingxi.service;

import com.lingxi.entity.Agent;
import com.lingxi.entity.User;
import com.lingxi.repository.AgentRepository;
import com.lingxi.repository.ChatHistoryRepository;
import com.lingxi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 智能体管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final UserRepository userRepository;
    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * 创建智能体
     */
    @Transactional
    public Agent createAgent(Agent agent, Long creatorId) {
        log.info("Creating agent: {} by user: {}", agent.getName(), creatorId);
        
        // 验证创建者权限
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("创建者不存在"));
        
        if (!creator.getRole().equals(User.UserRole.DEVELOPER) && !creator.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("只有开发者和管理员可以创建智能体");
        }
        
        // 检查名称是否已存在
        if (agentRepository.existsByName(agent.getName())) {
            throw new RuntimeException("智能体名称已存在");
        }
        
        // 设置默认值
        agent.setCreator(creator);
        agent.setStatus(Agent.AgentStatus.PENDING);
        agent.setEnabled(false);
        agent.setTotalCalls(0L);
        agent.setSuccessCalls(0L);
        agent.setAverageResponseTime(0.0);
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        
        return agentRepository.save(agent);
    }
    
    /**
     * 更新智能体
     */
    @Transactional
    public Agent updateAgent(Long agentId, Agent updatedAgent, Long userId) {
        log.info("Updating agent: {} by user: {}", agentId, userId);
        
        Agent existingAgent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("智能体不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 权限检查：只有创建者或管理员可以更新
        if (!existingAgent.getCreator().getId().equals(userId) && !user.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("没有权限更新此智能体");
        }
        
        // 检查名称唯一性（如果名称发生了变化）
        if (!existingAgent.getName().equals(updatedAgent.getName())) {
            Optional<Agent> agentWithSameName = agentRepository.findByName(updatedAgent.getName());
            if (agentWithSameName.isPresent() && !agentWithSameName.get().getId().equals(agentId)) {
                throw new RuntimeException("智能体名称已存在，请使用其他名称");
            }
        }
        
        // 更新字段
        existingAgent.setName(updatedAgent.getName());
        existingAgent.setDescription(updatedAgent.getDescription());
        existingAgent.setModelId(updatedAgent.getModelId());
        existingAgent.setEndpoint(updatedAgent.getEndpoint());
        existingAgent.setConfig(updatedAgent.getConfig());
        existingAgent.setType(updatedAgent.getType());
        existingAgent.setPriority(updatedAgent.getPriority());
        existingAgent.setUpdatedAt(LocalDateTime.now());
        
        // 如果修改了关键配置，需要重新审核
        if (!existingAgent.getModelId().equals(updatedAgent.getModelId()) || 
            !existingAgent.getEndpoint().equals(updatedAgent.getEndpoint())) {
            existingAgent.setStatus(Agent.AgentStatus.PENDING);
            existingAgent.setEnabled(false);
        }
        
        Agent savedAgent = agentRepository.save(existingAgent);
        Hibernate.initialize(savedAgent.getCreator()); // 初始化懒加载的关联
        return savedAgent;
    }
    
    /**
     * 审核智能体
     */
    @Transactional
    public Agent reviewAgent(Long agentId, Agent.AgentStatus status, String reviewComment, Long reviewerId) {
        log.info("Reviewing agent: {} with status: {} by reviewer: {}", agentId, status, reviewerId);
        
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("智能体不存在"));
        
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("审核者不存在"));
        
        // 权限检查：只有管理员可以审核
        if (!reviewer.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("只有管理员可以审核智能体");
        }
        
        agent.setStatus(status);
        agent.setApprovalNotes(reviewComment);
        agent.setApprovalTime(LocalDateTime.now());
        agent.setApprover(reviewer);
        agent.setUpdatedAt(LocalDateTime.now());
        
        // 如果审核通过，启用智能体
        if (status == Agent.AgentStatus.APPROVED) {
            agent.setEnabled(true);
        } else {
            agent.setEnabled(false);
        }
        
        return agentRepository.save(agent);
    }
    
    /**
     * 启用/禁用智能体
     */
    @Transactional
    public Agent toggleAgentStatus(Long agentId, boolean enabled, Long userId) {
        log.info("Toggling agent: {} status to: {} by user: {}", agentId, enabled, userId);
        
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("智能体不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 权限检查
        if (!agent.getCreator().getId().equals(userId) && !user.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("没有权限修改此智能体状态");
        }
        
        // 只有已审核通过的智能体才能启用
        if (enabled && !agent.getStatus().equals(Agent.AgentStatus.APPROVED)) {
            throw new RuntimeException("只有审核通过的智能体才能启用");
        }
        
        agent.setEnabled(enabled);
        agent.setUpdatedAt(LocalDateTime.now());
        
        return agentRepository.save(agent);
    }
    
    /**
     * 删除智能体
     */
    @Transactional
    public void deleteAgent(Long agentId, Long userId) {
        log.info("Deleting agent: {} by user: {}", agentId, userId);
        
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("智能体不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 权限检查
        if (!agent.getCreator().getId().equals(userId) && !user.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("没有权限删除此智能体");
        }
        
        // 先删除相关的聊天历史记录，避免外键约束冲突
        chatHistoryRepository.deleteChatHistoryByAgentId(agentId);
        
        agentRepository.delete(agent);
    }
    
    /**
     * 获取智能体详情
     */
    public Optional<Agent> getAgentById(Long agentId) {
        return agentRepository.findById(agentId);
    }
    
    /**
     * 根据名称获取智能体
     */
    public Optional<Agent> getAgentByName(String name) {
        return agentRepository.findByName(name);
    }

    /**
     * 获取所有已批准的智能体（分页）
     */
    public Page<Agent> getApprovedAgents(Pageable pageable) {
        return agentRepository.findApprovedAgents(pageable);
    }

    
    /**
     * 获取所有活跃的智能体
     */
    public List<Agent> getActiveAgents() {
        return agentRepository.findActiveAgents();
    }
    
    /**
     * 获取待审核的智能体
     */
    public List<Agent> getPendingAgents() {
        return agentRepository.findPendingAgents();
    }
    
    /**
     * 分页获取待审核的智能体
     */
    public Page<Agent> getPendingAgents(Pageable pageable) {
        return agentRepository.findPendingAgents(pageable);
    }
    
    /**
     * 获取用户创建的智能体
     */
    public List<Agent> getAgentsByCreator(Long creatorId) {
        return agentRepository.findByCreator_Id(creatorId);
    }
    
    /**
     * 分页查询智能体
     */
    public Page<Agent> getAgentsByKeyword(String keyword, Pageable pageable) {
        return agentRepository.findByKeyword(keyword, pageable);
    }
    

    
    /**
     * 记录智能体调用
     */
    @Transactional
    public void recordAgentCall(Long agentId, Long responseTime, boolean success) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("智能体不存在"));
        
        // 更新调用统计
        agent.setTotalCalls(agent.getTotalCalls() + 1);
        
        if (success) {
            agent.setSuccessCalls(agent.getSuccessCalls() + 1);
        }
        
        // 更新平均响应时间
        if (responseTime != null && responseTime > 0) {
            agent.updateResponseTime(responseTime);
        }
        
        // 更新最后调用时间
        agent.setLastCallTime(LocalDateTime.now());
        
        agent.setUpdatedAt(LocalDateTime.now());
        
        agentRepository.save(agent);
    }
    
    /**
     * 更新智能体评分
     */
    @Transactional
    public void updateAgentRating(Long agentId, Double rating) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("智能体不存在"));
        
        // 评分功能暂时移除，因为Agent实体中没有相关字段
        // 可以在后续版本中添加评分相关的字段和逻辑
        
        agent.setUpdatedAt(LocalDateTime.now());
        agentRepository.save(agent);
    }
    
    /**
     * 重置今日调用统计
     */
    @Transactional
    public void resetTodayStats() {
        log.info("Resetting today's call statistics for all agents");
        
        List<Agent> allAgents = agentRepository.findAll();
        for (Agent agent : allAgents) {
            // 重置统计数据的功能可以根据需要实现
            // agent.setTotalCalls(0L);
            agent.setUpdatedAt(LocalDateTime.now());
        }
        
        agentRepository.saveAll(allAgents);
    }
    
    /**
     * 获取智能体统计信息
     */
    public List<Object[]> getAgentStatsByStatus() {
        return agentRepository.countAgentsByStatus();
    }
    
    /**
     * 获取智能体类型统计
     */
    public List<Object[]> getAgentStatsByType() {
        return agentRepository.countAgentsByType();
    }
    
    /**
     * 检查智能体健康状态
     */
    public List<Agent> checkAgentHealth() {
        // 查找错误率高的智能体
        List<Agent> highErrorAgents = agentRepository.findHighErrorRateAgents(0.1); // 10%错误率
        
        // 查找响应时间慢的智能体
        List<Agent> slowAgents = agentRepository.findSlowResponseAgents(5000L); // 5秒
        
        highErrorAgents.addAll(slowAgents);
        return highErrorAgents;
    }
    
    /**
     * 获取智能体能力列表
     */
    public List<Agent> getAgentsByCapability(String capability) {
        return agentRepository.findAgentsByCapability(capability);
    }
    
    /**
     * 验证智能体配置
     */
    public boolean validateAgentConfig(Agent agent) {
        // 验证必要字段
        if (agent.getName() == null || agent.getName().trim().isEmpty()) {
            return false;
        }
        
        if (agent.getModelId() == null || agent.getModelId().trim().isEmpty()) {
            return false;
        }
        
        if (agent.getEndpoint() == null || agent.getEndpoint().trim().isEmpty()) {
            return false;
        }
        
        // 验证端点格式
        if (!agent.getEndpoint().startsWith("http://") && !agent.getEndpoint().startsWith("https://")) {
            return false;
        }
        
        return true;
    }
}