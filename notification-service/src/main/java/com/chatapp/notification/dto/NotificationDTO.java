package com.chatapp.notification.dto;

import com.chatapp.notification.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String id;
    private Long userId;
    private Notification.NotificationType type;
    private String title;
    private String content;
    private String referenceId;
    private Notification.NotificationStatus status;
    private LocalDateTime createTime;
    
    // 辅助方法：从实体转换为DTO
    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .referenceId(notification.getReferenceId())
                .status(notification.getStatus())
                .createTime(notification.getCreateTime())
                .build();
    }
    
    // 辅助方法：从DTO转换为实体
    public Notification toEntity() {
        return Notification.builder()
                .id(this.id)
                .userId(this.userId)
                .type(this.type)
                .title(this.title)
                .content(this.content)
                .referenceId(this.referenceId)
                .status(this.status)
                .createTime(this.createTime != null ? this.createTime : LocalDateTime.now())
                .build();
    }
} 