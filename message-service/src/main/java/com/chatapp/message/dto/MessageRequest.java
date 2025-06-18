package com.chatapp.message.dto;

import com.chatapp.message.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 消息发送请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    @NotNull(message = "内容类型不能为空")
    private Message.ContentType contentType;

    @NotNull(message = "消息类型不能为空")
    private Message.MessageType messageType;

    /**
     * 附件URL列表，可选
     */
    private List<String> attachments;
} 