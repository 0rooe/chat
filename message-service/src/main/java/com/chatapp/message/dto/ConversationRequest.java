package com.chatapp.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 会话请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRequest {
    
    @NotNull(message = "接收者ID不能为空")
    private Long recipientId;
    
    private String title;
    
    private String avatar;
} 