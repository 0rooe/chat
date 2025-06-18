package com.chatapp.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    
    // 接收通知的用户ID
    private Long userId;
    
    // 通知类型
    private NotificationType type;
    
    // 通知标题
    private String title;
    
    // 通知内容
    private String content;
    
    // 相关数据ID（如消息ID、好友请求ID等）
    private String referenceId;
    
    // 通知状态
    private NotificationStatus status;
    
    // 创建时间
    private LocalDateTime createTime;
    
    public enum NotificationType {
        MESSAGE, // 新消息通知
        FRIEND_REQUEST, // 好友请求通知
        FRIEND_ACCEPT, // 好友接受通知
        GROUP_INVITE, // 群组邀请通知
        SYSTEM // 系统通知
    }
    
    public enum NotificationStatus {
        UNREAD, // 未读
        READ // 已读
    }
} 