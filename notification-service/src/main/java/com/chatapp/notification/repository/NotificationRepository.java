package com.chatapp.notification.repository;

import com.chatapp.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    // 查询用户的所有通知
    List<Notification> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    // 分页查询用户的所有通知
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    
    // 查询用户的未读通知
    List<Notification> findByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
    
    // 统计用户的未读通知数量
    long countByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
    
    // 根据类型查询用户的通知
    List<Notification> findByUserIdAndType(Long userId, Notification.NotificationType type);
} 