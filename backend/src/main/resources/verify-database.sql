-- 验证数据库表结构和字段的SQL脚本
-- 检查智能体相关表是否包含九天平台所需的字段

-- 1. 检查agents表结构
SELECT 
    'agents' as table_name,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'agents'
    AND COLUMN_NAME IN ('app_id', 'api_key', 'token', 'name', 'description', 'endpoint')
ORDER BY ORDINAL_POSITION;

-- 2. 检查agent_submissions表结构
SELECT 
    'agent_submissions' as table_name,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'agent_submissions'
    AND COLUMN_NAME IN ('app_id', 'api_key', 'token', 'name', 'description', 'api_url')
ORDER BY ORDINAL_POSITION;

-- 3. 检查当前数据量
SELECT 
    'agents' as table_name, 
    COUNT(*) as record_count,
    COUNT(CASE WHEN app_id IS NOT NULL THEN 1 END) as with_app_id,
    COUNT(CASE WHEN api_key IS NOT NULL THEN 1 END) as with_api_key,
    COUNT(CASE WHEN token IS NOT NULL THEN 1 END) as with_token
FROM agents
UNION ALL
SELECT 
    'agent_submissions' as table_name, 
    COUNT(*) as record_count,
    COUNT(CASE WHEN app_id IS NOT NULL THEN 1 END) as with_app_id,
    COUNT(CASE WHEN api_key IS NOT NULL THEN 1 END) as with_api_key,
    COUNT(CASE WHEN token IS NOT NULL THEN 1 END) as with_token
FROM agent_submissions;

-- 4. 检查相关表的外键关系
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = DATABASE() 
    AND REFERENCED_TABLE_NAME IN ('agents', 'agent_submissions')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 5. 显示所有智能体相关表的索引
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME IN ('agents', 'agent_submissions', 'agent_config', 'agent_tags')
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;