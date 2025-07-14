-- 九天平台数据库初始化脚本
-- 确保数据库表结构支持appId、apiKey、token字段
-- 清理所有现有智能体数据

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 清理现有智能体相关数据
-- ============================================

-- 清理聊天历史记录（与智能体相关的）
DELETE FROM chat_history WHERE agent_id IS NOT NULL;

-- 清理对话日志（与智能体相关的）
DELETE FROM conversation_logs WHERE agent_id IS NOT NULL;

-- 清理智能体配置参数
DELETE FROM agent_config;

-- 清理智能体标签
DELETE FROM agent_tags;

-- 清理智能体提交申请
DELETE FROM agent_submissions;

-- 清理智能体数据
DELETE FROM agents;

-- ============================================
-- 2. 验证和创建表结构（如果不存在）
-- ============================================

-- 创建agents表（如果不存在）
CREATE TABLE IF NOT EXISTS `agents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '智能体名称',
  `description` varchar(500) DEFAULT NULL COMMENT '智能体描述',
  `model_id` varchar(100) NOT NULL COMMENT '模型ID',
  `endpoint` varchar(200) NOT NULL COMMENT 'API端点',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像URL',
  `app_id` varchar(100) DEFAULT NULL COMMENT '九天平台应用ID',
  `api_key` varchar(500) DEFAULT NULL COMMENT '九天平台API密钥',
  `token` varchar(1000) DEFAULT NULL COMMENT '九天平台Token',
  `status` enum('PENDING','APPROVED','REJECTED','SUSPENDED') NOT NULL DEFAULT 'PENDING' COMMENT '状态',
  `type` enum('JIUTIAN','LOCAL','CUSTOM') NOT NULL DEFAULT 'JIUTIAN' COMMENT '类型',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `approver_id` bigint DEFAULT NULL COMMENT '审核者ID',
  `approval_time` datetime DEFAULT NULL COMMENT '审核时间',
  `approval_notes` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `total_calls` bigint NOT NULL DEFAULT '0' COMMENT '总调用次数',
  `success_calls` bigint NOT NULL DEFAULT '0' COMMENT '成功调用次数',
  `last_call_time` datetime DEFAULT NULL COMMENT '最后调用时间',
  `average_response_time` double DEFAULT NULL COMMENT '平均响应时间',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_name` (`name`),
  KEY `idx_agent_status` (`status`),
  KEY `idx_agent_creator` (`creator_id`),
  KEY `idx_agent_type` (`type`),
  KEY `idx_agent_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体表';

-- 创建agent_submissions表（如果不存在）
CREATE TABLE IF NOT EXISTS `agent_submissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '智能体名称',
  `description` varchar(500) NOT NULL COMMENT '智能体描述',
  `api_url` varchar(200) NOT NULL COMMENT 'API地址',
  `app_id` varchar(100) NOT NULL COMMENT '九天平台应用ID',
  `api_key` varchar(500) NOT NULL COMMENT '九天平台API密钥',
  `token` varchar(1000) NOT NULL COMMENT '九天平台Token',
  `category` varchar(50) NOT NULL COMMENT '智能体类别',
  `submitter_id` bigint NOT NULL COMMENT '提交者ID',
  `submitter_role` enum('STUDENT','TEACHER','DEVELOPER','ADMIN') NOT NULL COMMENT '提交者角色',
  `status` enum('PENDING','APPROVED','REJECTED','TESTING') NOT NULL DEFAULT 'PENDING' COMMENT '提交状态',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核者ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `review_notes` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `agent_id` bigint DEFAULT NULL COMMENT '审核通过后创建的智能体ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_submission_submitter` (`submitter_id`),
  KEY `idx_submission_status` (`status`),
  KEY `idx_submission_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体提交申请表';

-- 创建agent_config表（如果不存在）
CREATE TABLE IF NOT EXISTS `agent_config` (
  `agent_id` bigint NOT NULL,
  `config_key` varchar(255) NOT NULL,
  `config_value` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`agent_id`,`config_key`),
  CONSTRAINT `fk_agent_config_agent` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体配置参数表';

-- 创建agent_tags表（如果不存在）
CREATE TABLE IF NOT EXISTS `agent_tags` (
  `agent_id` bigint NOT NULL,
  `tag` varchar(255) NOT NULL,
  PRIMARY KEY (`agent_id`,`tag`),
  CONSTRAINT `fk_agent_tags_agent` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体标签表';

-- ============================================
-- 3. 重置自增ID
-- ============================================

ALTER TABLE agents AUTO_INCREMENT = 1;
ALTER TABLE agent_submissions AUTO_INCREMENT = 1;

-- ============================================
-- 4. 验证表结构
-- ============================================

-- 检查agents表的九天字段
SELECT 
    'agents表字段检查' as check_type,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'agents'
    AND COLUMN_NAME IN ('app_id', 'api_key', 'token')
ORDER BY COLUMN_NAME;

-- 检查agent_submissions表的九天字段
SELECT 
    'agent_submissions表字段检查' as check_type,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'agent_submissions'
    AND COLUMN_NAME IN ('app_id', 'api_key', 'token')
ORDER BY COLUMN_NAME;

-- ============================================
-- 5. 验证清理结果
-- ============================================

SELECT 
    'agents' as table_name, 
    COUNT(*) as record_count
FROM agents
UNION ALL
SELECT 
    'agent_submissions' as table_name, 
    COUNT(*) as record_count
FROM agent_submissions
UNION ALL
SELECT 
    'agent_config' as table_name, 
    COUNT(*) as record_count
FROM agent_config
UNION ALL
SELECT 
    'agent_tags' as table_name, 
    COUNT(*) as record_count
FROM agent_tags;

-- 恢复外键约束检查
SET FOREIGN_KEY_CHECKS = 1;

-- 显示完成信息
SELECT 
    '九天平台数据库初始化完成' as status,
    '所有智能体数据已清理' as cleanup_status,
    '数据库表结构已验证' as structure_status,
    NOW() as completion_time;