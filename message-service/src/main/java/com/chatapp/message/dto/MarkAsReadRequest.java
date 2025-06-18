package com.chatapp.message.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 标记消息已读请求DTO
 */
@Data
public class MarkAsReadRequest {
    
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;
    
    @NotNull(message = "发送者ID不能为空") 
    private Long senderId;
} 