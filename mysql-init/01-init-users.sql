-- 创建用户数据库
CREATE DATABASE IF NOT EXISTS chat_user_db;
USE chat_user_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    avatar VARCHAR(200),
    status ENUM('ONLINE', 'OFFLINE', 'BUSY', 'AWAY') DEFAULT 'OFFLINE',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_status (status)
);

-- 插入测试用户数据
INSERT INTO users (username, nickname, password, status) VALUES
('clq', '橙留香', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ONLINE'),
('clq2', '橙留香2号', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ONLINE'),
('clq3', '橙留香3号', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ONLINE'),
('xman', 'X战警', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ONLINE'),
('long', '龙哥', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ONLINE')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname); 