#!/bin/bash
set -e

echo "Creating MySQL databases..."

# 创建多个数据库
mysql -u root -p"$MYSQL_ROOT_PASSWORD" <<-EOSQL
    CREATE DATABASE IF NOT EXISTS chat_user_db;
    CREATE DATABASE IF NOT EXISTS chat_relationship_db;
    GRANT ALL PRIVILEGES ON chat_user_db.* TO 'root'@'%';
    GRANT ALL PRIVILEGES ON chat_relationship_db.* TO 'root'@'%';
    FLUSH PRIVILEGES;
EOSQL

echo "MySQL databases created successfully!" 