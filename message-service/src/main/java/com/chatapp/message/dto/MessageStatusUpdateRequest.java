package com.chatapp.message.dto;

import com.chatapp.message.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 消息状态更新请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatusUpdateRequest {

    @NotNull(message = "消息状态不能为空")
    private Message.MessageStatus status;
} 