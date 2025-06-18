-- 06-add-group-members-fields.sql
-- 为group_members表添加缺失的字段

USE chat_relationship_db;

-- 修改role枚举，添加OWNER选项
ALTER TABLE group_members MODIFY COLUMN role ENUM('OWNER', 'ADMIN', 'MEMBER') DEFAULT 'MEMBER';

-- 添加last_active_time字段
ALTER TABLE group_members ADD COLUMN IF NOT EXISTS last_active_time TIMESTAMP NULL;

-- 添加muted字段
ALTER TABLE group_members ADD COLUMN IF NOT EXISTS muted BOOLEAN DEFAULT FALSE;

-- 更新现有记录的last_active_time为join_time
UPDATE group_members SET last_active_time = join_time WHERE last_active_time IS NULL; 