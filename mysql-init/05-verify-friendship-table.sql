-- 验证并修复friendship表结构
USE chat_relationship_db;

-- 显示当前表结构
SHOW CREATE TABLE friendships;

-- 如果表结构不正确，删除并重新创建
DROP TABLE IF EXISTS friendships;

-- 重新创建表，确保字段名与实体模型完全匹配
CREATE TABLE friendships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    addressee_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'BLOCKED') DEFAULT 'PENDING',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_friendship (requester_id, addressee_id),
    INDEX idx_requester (requester_id),
    INDEX idx_addressee (addressee_id),
    INDEX idx_status (status)
);

-- 插入测试数据
INSERT INTO friendships (requester_id, addressee_id, status) VALUES
(1, 2, 'ACCEPTED'),
(1, 3, 'PENDING')
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- 验证表结构
DESCRIBE friendships; 