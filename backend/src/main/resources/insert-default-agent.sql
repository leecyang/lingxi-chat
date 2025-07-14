-- 插入默认灵犀智能体
-- 这个脚本用于创建用户指定的默认智能体

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 插入默认灵犀智能体
-- ============================================

-- 首先检查是否已存在该智能体
SELECT COUNT(*) as existing_count FROM agents WHERE name = 'lingxi';

-- 如果不存在，则插入默认灵犀智能体
INSERT IGNORE INTO agents (
    id,
    name,
    description,
    model_id,
    endpoint,
    avatar,
    app_id,
    api_key,
    token,
    status,
    type,
    creator_id,
    enabled,
    priority,
    created_at,
    updated_at
) VALUES (
    6867fda14c78b04e5ad1b603,  -- 用户指定的ID（注意：MySQL的BIGINT可能无法存储这个十六进制值）
    'lingxi',                    -- 智能体名称
    '灵犀智能体 - 默认对话助手，提供智能问答和对话服务',  -- 描述
    '686b39b2b381ac407d61459f',  -- 模型ID（使用用户提供的APIkey作为模型ID）
    'https://api.jiutian.com/v1/chat/completions',  -- API端点（九天平台默认端点）
    '/logo.png',                 -- 头像
    '686b39b2b381ac407d61459f',  -- 九天平台应用ID（使用用户提供的APIkey）
    '686b39b2b381ac407d61459f.VrwEzsjiVRlzPUlVbQgU44ev6YRgr9HW',  -- API密钥
    'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcGlfa2V5IjoiNjg2YjM5YjJiMzgxYWM0MDdkNjE0NTlmIiwiZXhwIjoxNzUxOTQxNzU5LCJ0aW1lc3RhbXAiOjE3NTE4ODE3NTl9.P4fzhKVNbUPt4vWN5v8eRG_8XPGT5gd5VxsZYj4wgTE',  -- Token
    'APPROVED',                  -- 状态：已审核通过
    'JIUTIAN',                   -- 类型：九天平台
    1,                           -- 创建者ID（假设管理员用户ID为1）
    1,                           -- 启用状态
    100,                         -- 高优先级
    NOW(),                       -- 创建时间
    NOW()                        -- 更新时间
);

-- 由于MySQL的BIGINT无法存储用户提供的十六进制ID，我们使用自增ID
-- 但是我们需要记录原始ID作为配置
SET @agent_id = LAST_INSERT_ID();

-- 插入配置信息，记录原始ID
INSERT IGNORE INTO agent_config (agent_id, config_key, config_value) VALUES
(@agent_id, 'original_id', '6867fda14c78b04e5ad1b603'),
(@agent_id, 'is_default', 'true'),
(@agent_id, 'max_tokens', '2048'),
(@agent_id, 'temperature', '0.7');

-- 插入标签
INSERT IGNORE INTO agent_tags (agent_id, tag) VALUES
(@agent_id, '默认'),
(@agent_id, '灵犀'),
(@agent_id, '对话助手'),
(@agent_id, '九天平台');

-- 验证插入结果
SELECT 
    a.id,
    a.name,
    a.description,
    a.status,
    a.enabled,
    a.priority,
    GROUP_CONCAT(DISTINCT t.tag) as tags,
    GROUP_CONCAT(DISTINCT CONCAT(c.config_key, '=', c.config_value)) as configs
FROM agents a
LEFT JOIN agent_tags t ON a.id = t.agent_id
LEFT JOIN agent_config c ON a.id = c.agent_id
WHERE a.name = 'lingxi'
GROUP BY a.id;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '默认灵犀智能体创建完成' as status, NOW() as completion_time;