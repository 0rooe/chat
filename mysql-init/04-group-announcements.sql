-- 创建群公告表
CREATE TABLE IF NOT EXISTS group_announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    publisher_id BIGINT NOT NULL,
    publisher_name VARCHAR(100),
    publish_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_group_id (group_id),
    INDEX idx_publisher_id (publisher_id),
    INDEX idx_publish_time (publish_time),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群公告表'; 