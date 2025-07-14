-- Remove display_name column from agents table
-- Use unified name field to simplify agent configuration

SET FOREIGN_KEY_CHECKS = 0;

-- Drop display_name column (ignore error if column does not exist)
ALTER TABLE agents DROP COLUMN display_name;

-- Modify name field length to 100 to support longer agent names
ALTER TABLE agents MODIFY COLUMN name VARCHAR(100) NOT NULL;

SET FOREIGN_KEY_CHECKS = 1;

-- Verify modification results
SELECT 
    'agents_table_field_check' as check_type,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'agents'
    AND COLUMN_NAME = 'name'
ORDER BY COLUMN_NAME;

SELECT 'Agent table structure optimization completed' as status, NOW() as completion_time;