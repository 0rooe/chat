package com.chatapp.message.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    
    private Long senderId;
    
    private Long receiverId;
    
    private String content;
    
    private ContentType contentType;
    
    private MessageType messageType;
    
    private MessageStatus status;
    
    private boolean isEncrypted;
    
    @Field("create_time")
    private LocalDateTime createTime;
    
    @Field("update_time")
    private LocalDateTime updateTime;
    
    private List<String> attachments;
    
    public enum ContentType {
        TEXT, IMAGE, FILE, AUDIO, VIDEO
    }
    
    public enum MessageType {
        PRIVATE, GROUP
    }
    
    public enum MessageStatus {
        SENDING, SENT, DELIVERED, READ, FAILED
    }
} 