package com.chatapp.message.dto;

import com.chatapp.message.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private String id;

    @NotNull(message = "发送者ID不能为空")
    private Long senderId;

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    @NotNull(message = "内容类型不能为空")
    private String contentType;

    @NotNull(message = "消息类型不能为空")
    private String messageType;

    private String status;

    private boolean encrypted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private List<String> attachments;
    
    // 额外的字段，用于前端展示
    private String senderName;
    private String senderAvatar;
    private String receiverName;
    private String receiverAvatar;

    /**
     * DTO转实体
     */
    public Message toEntity() {
        Message.ContentType contentTypeEnum = null;
        if (this.contentType != null) {
            try {
                contentTypeEnum = Message.ContentType.valueOf(this.contentType);
            } catch (IllegalArgumentException e) {
                contentTypeEnum = Message.ContentType.TEXT;
            }
        }
        
        Message.MessageType messageTypeEnum = null;
        if (this.messageType != null) {
            try {
                messageTypeEnum = Message.MessageType.valueOf(this.messageType);
            } catch (IllegalArgumentException e) {
                messageTypeEnum = Message.MessageType.PRIVATE;
            }
        }
        
        Message.MessageStatus statusEnum = null;
        if (this.status != null) {
            try {
                statusEnum = Message.MessageStatus.valueOf(this.status);
            } catch (IllegalArgumentException e) {
                statusEnum = Message.MessageStatus.SENDING;
            }
        }
        
        return Message.builder()
                .id(this.id)
                .senderId(this.senderId)
                .receiverId(this.receiverId)
                .content(this.content)
                .contentType(contentTypeEnum)
                .messageType(messageTypeEnum)
                .status(statusEnum)
                .isEncrypted(this.encrypted)
                .createTime(this.createTime)
                .updateTime(this.updateTime)
                .attachments(this.attachments)
                .build();
    }

    /**
     * 实体转DTO
     */
    public static MessageDTO fromEntity(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .contentType(message.getContentType() != null ? message.getContentType().name() : null)
                .messageType(message.getMessageType() != null ? message.getMessageType().name() : null)
                .status(message.getStatus() != null ? message.getStatus().name() : null)
                .encrypted(message.isEncrypted())
                .createTime(message.getCreateTime())
                .updateTime(message.getUpdateTime())
                .attachments(message.getAttachments())
                .build();
    }

    /**
     * 批量将实体对象转换为DTO
     */
    public static List<MessageDTO> fromEntities(List<Message> messages) {
        if (messages == null) {
            return new ArrayList<>();
        }
        return messages.stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());
    }
} 