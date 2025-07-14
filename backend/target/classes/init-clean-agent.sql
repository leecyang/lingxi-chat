-- Clean and reinitialize agent database configuration
-- Use unified name field, remove displayName distinction
-- Ensure API request information is configurable rather than hardcoded

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. Completely clean existing agent-related data
-- ============================================

-- Clean chat history (agent-related)
DELETE FROM chat_history WHERE agent_id IS NOT NULL;

-- Clean conversation logs (agent-related)
DELETE FROM conversation_logs WHERE agent_id IS NOT NULL;

-- Clean agent configuration parameters
DELETE FROM agent_config;

-- Clean agent tags
DELETE FROM agent_tags;

-- Clean agent submissions
DELETE FROM agent_submissions;

-- Clean agent data
DELETE FROM agents;

-- Reset auto-increment IDs
ALTER TABLE agents AUTO_INCREMENT = 1;
ALTER TABLE agent_submissions AUTO_INCREMENT = 1;

-- ============================================
-- 2. Create standard agent configuration
-- ============================================

-- Insert Jiutian agent (using unified name field)
INSERT INTO agents (
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
    total_calls,
    success_calls,
    category,
    created_at,
    updated_at
) VALUES (
    'jiutian-assistant',
    'Jiutian AI Assistant - General conversation assistant based on Jiutian platform',
    'jiutian-lan',
    'https://api.jiutian.com/v1/chat/completions',
    '/logo.png',
    '686b39b2b381ac407d61459f',
    '686b39b2b381ac407d61459f.VrwEzsjiVRlzPUlVbQgU44ev6YRgr9HW',
    'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcGlfa2V5IjoiNjg2YjM5YjJiMzgxYWM0MDdkNjE0NTlmIiwiZXhwIjoxNzUxOTQxNzU5LCJ0aW1lc3RhbXAiOjE3NTE4ODE3NTl9.P4fzhKVNbUPt4vWN5v8eRG_8XPGT5gd5VxsZYj4wgTE',
    'APPROVED',
    'JIUTIAN',
    1,
    1,
    100,
    0,
    0,
    'GENERAL_AI',
    NOW(),
    NOW()
);

-- Get the ID of the just inserted agent
SET @agent_id = LAST_INSERT_ID();

-- Insert agent configuration parameters
INSERT INTO agent_config (agent_id, config_key, config_value) VALUES
(@agent_id, 'max_tokens', '2048'),
(@agent_id, 'temperature', '0.7'),
(@agent_id, 'top_p', '0.9'),
(@agent_id, 'is_default', 'true'),
(@agent_id, 'system_prompt', 'You are an intelligent assistant. Please answer user questions in a friendly and accurate manner.');

-- Insert agent tags
INSERT INTO agent_tags (agent_id, tag) VALUES
(@agent_id, 'default'),
(@agent_id, 'jiutian-platform'),
(@agent_id, 'general-assistant'),
(@agent_id, 'conversation');

-- ============================================
-- 3. Verify configuration results
-- ============================================

-- Display created agent information
SELECT 
    a.id,
    a.name,
    a.description,
    a.model_id,
    a.endpoint,
    a.app_id,
    CONCAT(LEFT(a.api_key, 10), '...') as api_key_preview,
    CONCAT(LEFT(a.token, 20), '...') as token_preview,
    a.status,
    a.type,
    a.enabled,
    a.priority,
    GROUP_CONCAT(DISTINCT t.tag) as tags,
    GROUP_CONCAT(DISTINCT CONCAT(c.config_key, '=', c.config_value)) as configs
FROM agents a
LEFT JOIN agent_tags t ON a.id = t.agent_id
LEFT JOIN agent_config c ON a.id = c.agent_id
WHERE a.name = 'jiutian-assistant'
GROUP BY a.id;

-- Verify table record counts
SELECT 
    'agents' as table_name, 
    COUNT(*) as record_count
FROM agents
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

-- Restore foreign key constraint checking
SET FOREIGN_KEY_CHECKS = 1;

-- Display completion information
SELECT 
    'Agent database cleanup and reconfiguration completed' as status,
    'Standard Jiutian agent configuration created' as agent_status,
    'All API information configurable through database' as config_status,
    NOW() as completion_time;