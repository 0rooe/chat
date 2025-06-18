package com.chatapp.message.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 会话实体
 */
@Document(collection = "conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "userId_friendId", def = "{'userId': 1, 'friendId': 1}", unique = true)
})
public class Conversation {
    
    @Id
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
    
    private LocalDateTime updateTime;
} 