package com.chatapp.notification.service;

import com.chatapp.notification.dto.NotificationDTO;
import com.chatapp.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    
    // 创建新通知
    NotificationDTO createNotification(NotificationDTO notificationDTO);
    
    // 获取用户的所有通知
    List<NotificationDTO> getUserNotifications(Long userId);
    
    // 分页获取用户通知
    Page<NotificationDTO> getUserNotificationsPaged(Long userId, Pageable pageable);
    
    // 获取用户的未读通知
    List<NotificationDTO> getUnreadNotifications(Long userId);
    
    // 统计用户的未读通知数量
    long countUnreadNotifications(Long userId);
    
    // 将通知标记为已读
    NotificationDTO markAsRead(String notificationId);
    
    // 将用户的所有通知标记为已读
    void markAllAsRead(Long userId);
    
    // 删除通知
    void deleteNotification(String notificationId);
    
    // 发送即时通知（通过WebSocket）
    void sendInstantNotification(NotificationDTO notificationDTO);
} 