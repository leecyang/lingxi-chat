-- 更新灵犀智能体的API端点
UPDATE agents 
SET endpoint = 'https://api.lingxi.ai/v1/chat/completions',
    updated_at = NOW()
WHERE name = 'lingxi';

-- 验证更新结果
SELECT id, name, endpoint, updated_at 
FROM agents 
WHERE name = 'lingxi';