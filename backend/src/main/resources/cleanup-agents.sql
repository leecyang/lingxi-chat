-- 清理智能体相关数据的SQL脚本
-- 注意：此脚本会删除所有智能体相关的数据，请谨慎使用

-- 禁用外键约束检查（MySQL）
SET FOREIGN_KEY_CHECKS = 0;

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

-- 重置自增ID（MySQL）
ALTER TABLE agents AUTO_INCREMENT = 1;
ALTER TABLE agent_submissions AUTO_INCREMENT = 1;
ALTER TABLE agent_config AUTO_INCREMENT = 1;
ALTER TABLE agent_tags AUTO_INCREMENT = 1;

-- 启用外键约束检查
SET FOREIGN_KEY_CHECKS = 1;

-- 验证清理结果
SELECT 'agents' as table_name, COUNT(*) as record_count FROM agents
UNION ALL
SELECT 'agent_submissions' as table_name, COUNT(*) as record_count FROM agent_submissions
UNION ALL
SELECT 'agent_config' as table_name, COUNT(*) as record_count FROM agent_config
UNION ALL
SELECT 'agent_tags' as table_name, COUNT(*) as record_count FROM agent_tags
UNION ALL
SELECT 'chat_history_with_agents' as table_name, COUNT(*) as record_count FROM chat_history WHERE agent_id IS NOT NULL
UNION ALL
SELECT 'conversation_logs_with_agents' as table_name, COUNT(*) as record_count FROM conversation_logs WHERE agent_id IS NOT NULL;

-- 显示清理完成信息
SELECT '智能体相关数据清理完成' as status, NOW() as cleanup_time;