package com.chatapp.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 会话响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    
    private String id;
    
    private Long userId;
    
    private Long friendId;
    
    private String title;
    
    private String avatar;
    
    private LocalDateTime lastMessageTime;
    
    private String lastMessageContent;
    
    private Boolean unread;
    
    private Integer unreadCount;
    
    private LocalDateTime createTime;
    
    private Map<String, Object> friendInfo;
} 