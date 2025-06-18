-- 删除用户表中的email字段
USE chat_user_db;

-- 删除email字段
ALTER TABLE users DROP COLUMN email; 