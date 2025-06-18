package com.chatapp.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedMessageDto {
    private Long senderId;
    private Long receiverId;
    private String encryptedContent;
    private String contentType;
    private String messageType;
}