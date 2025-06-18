package com.chatapp.message.dto;

import com.chatapp.message.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息查询参数DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageQueryParams {

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息类型
     */
    private Message.MessageType messageType;

    /**
     * 开始时间戳（毫秒）
     */
    private Long startTime;

    /**
     * 结束时间戳（毫秒）
     */
    private Long endTime;

    /**
     * 页码，从0开始
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;
} 