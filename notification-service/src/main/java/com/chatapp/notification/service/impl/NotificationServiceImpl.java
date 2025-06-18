package com.chatapp.notification.service.impl;

import com.chatapp.notification.dto.NotificationDTO;
import com.chatapp.notification.model.Notification;
import com.chatapp.notification.repository.NotificationRepository;
import com.chatapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        if (notificationDTO.getCreateTime() == null) {
            notificationDTO.setCreateTime(LocalDateTime.now());
        }
        
        if (notificationDTO.getStatus() == null) {
            notificationDTO.setStatus(Notification.NotificationStatus.UNREAD);
        }
        
        Notification notification = notificationDTO.toEntity();
        notification = notificationRepository.save(notification);
        return NotificationDTO.fromEntity(notification);
    }
    
    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreateTimeDesc(userId);
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<NotificationDTO> getUserNotificationsPaged(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);
        return notifications.map(NotificationDTO::fromEntity);
    }
    
    @Override
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndStatus(
                userId, Notification.NotificationStatus.UNREAD);
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, Notification.NotificationStatus.UNREAD);
    }
    
    @Override
    public NotificationDTO markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在: " + notificationId));
        
        notification.setStatus(Notification.NotificationStatus.READ);
        notification = notificationRepository.save(notification);
        return NotificationDTO.fromEntity(notification);
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndStatus(
                userId, Notification.NotificationStatus.UNREAD);
        
        unreadNotifications.forEach(notification -> notification.setStatus(Notification.NotificationStatus.READ));
        notificationRepository.saveAll(unreadNotifications);
    }
    
    @Override
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    @Override
    public void sendInstantNotification(NotificationDTO notificationDTO) {
        // 保存通知到数据库
        NotificationDTO savedNotification = createNotification(notificationDTO);
        
        // 通过WebSocket发送即时通知
        messagingTemplate.convertAndSendToUser(
                String.valueOf(notificationDTO.getUserId()),
                "/queue/notifications",
                savedNotification
        );
        
        log.info("发送通知到用户 {}: {}", notificationDTO.getUserId(), notificationDTO.getTitle());
    }
} 