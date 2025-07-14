package com.lingxi.service;

import com.lingxi.entity.Agent;
import com.lingxi.entity.User;
import com.lingxi.repository.AgentRepository;
import com.lingxi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 默认智能体初始化服务
 * 在应用启动时自动创建默认的灵犀智能体
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAgentInitService implements ApplicationRunner {

    private final AgentRepository agentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化默认智能体...");
        createDefaultLingxiAgent();
        log.info("默认智能体初始化完成");
    }

    /**
     * 创建默认的灵犀智能体
     */
    private void createDefaultLingxiAgent() {
        // 检查是否已存在灵犀智能体
        if (agentRepository.findByName("lingxi").isPresent()) {
            log.info("灵犀智能体已存在，跳过创建");
            return;
        }

        try {
            // 查找管理员用户作为创建者（如果没有管理员，使用第一个用户）
            User creator = userRepository.findByRole(User.UserRole.ADMIN)
                    .stream()
                    .findFirst()
                    .orElse(userRepository.findAll().stream().findFirst().orElse(null));

            if (creator == null) {
                log.warn("未找到用户，无法创建默认智能体");
                return;
            }

            // 创建默认灵犀智能体 - 使用九天大模型配置
            Agent lingxiAgent = new Agent();
            lingxiAgent.setName("lingxi");
            lingxiAgent.setDisplayName("灵犀");
            lingxiAgent.setDescription("灵犀智能体 - 基于九天大模型的默认对话助手，提供智能问答和对话服务");
            lingxiAgent.setModelId("jiutian-lan");
            lingxiAgent.setEndpoint("https://jiutian.10086.cn/largemodel/api/v1/completions");
            lingxiAgent.setAvatar("/logo.png");
            // 注意：请替换为您的真实九天API Key（kid.secret格式）
            lingxiAgent.setApiKey("your_kid.secret_here");
            lingxiAgent.setToken(null); // Token由JiutianTokenService动态生成
            lingxiAgent.setStatus(Agent.AgentStatus.APPROVED);
            lingxiAgent.setType(Agent.AgentType.JIUTIAN);
            lingxiAgent.setCreator(creator);
            lingxiAgent.setApprover(creator);
            lingxiAgent.setApprovalTime(LocalDateTime.now());
            lingxiAgent.setApprovalNotes("系统默认智能体，自动审核通过");
            lingxiAgent.setEnabled(true);
            lingxiAgent.setPriority(100); // 高优先级
            lingxiAgent.setCategory(Agent.AgentCategory.COORDINATION_AGENT); // 设置为统筹智能体分类
            lingxiAgent.setTotalCalls(0L);
            lingxiAgent.setSuccessCalls(0L);

            // 设置配置参数
            Map<String, String> config = new HashMap<>();
            config.put("original_id", "6867fda14c78b04e5ad1b603");
            config.put("is_default", "true");
            config.put("max_tokens", "2048");
            config.put("temperature", "0.7");
            lingxiAgent.setConfig(config);

            // 设置标签
            Set<String> tags = new HashSet<>();
            tags.add("默认");
            tags.add("灵犀");
            tags.add("对话助手");
            tags.add("九天平台");
            lingxiAgent.setTags(tags);

            // 保存智能体
            Agent savedAgent = agentRepository.save(lingxiAgent);
            log.info("成功创建默认灵犀智能体，ID: {}, 名称: {}", savedAgent.getId(), savedAgent.getName());

        } catch (Exception e) {
            log.error("创建默认灵犀智能体失败", e);
        }
    }
}