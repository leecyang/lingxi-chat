-- 灵犀智学数据库初始化脚本
-- 清理现有智能体数据并重新初始化

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

-- 清理智能体表
DELETE FROM agents;

-- ============================================
-- 2. 重置自增ID
-- ============================================

ALTER TABLE agents AUTO_INCREMENT = 1;
ALTER TABLE agent_submissions AUTO_INCREMENT = 1;

-- ============================================
-- 3. 确保表结构正确
-- ============================================

-- 更新agents表结构，确保包含所有必要字段
ALTER TABLE agents 
MODIFY COLUMN `name` varchar(100) NOT NULL COMMENT '智能体名称',
MODIFY COLUMN `description` varchar(500) DEFAULT NULL COMMENT '智能体描述',
MODIFY COLUMN `model_id` varchar(100) NOT NULL COMMENT '模型ID',
MODIFY COLUMN `endpoint` varchar(200) DEFAULT NULL COMMENT 'API端点',
MODIFY COLUMN `api_key` varchar(500) DEFAULT NULL COMMENT 'API密钥',
MODIFY COLUMN `token` varchar(1000) DEFAULT NULL COMMENT '访问令牌',
MODIFY COLUMN `category` enum('MORAL_EDUCATION','INTELLECTUAL_EDUCATION','PHYSICAL_EDUCATION','AESTHETIC_EDUCATION','LABOR_EDUCATION','LIFE_SUPPORT','GENERAL_AI') NOT NULL DEFAULT 'GENERAL_AI' COMMENT '智能体类别';

-- 创建索引（忽略重复错误）
CREATE INDEX idx_agents_model_id ON agents(model_id);
CREATE INDEX idx_agents_category ON agents(category);
CREATE INDEX idx_agents_status ON agents(status);

SET FOREIGN_KEY_CHECKS = 1;

-- 显示初始化完成信息
SELECT '数据库初始化完成，智能体数据已清理' as status, NOW() as completion_time;