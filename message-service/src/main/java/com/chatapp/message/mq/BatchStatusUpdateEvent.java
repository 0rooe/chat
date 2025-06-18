package com.chatapp.message.mq;

import com.chatapp.message.model.Message;
import java.util.List;

/**
 * 批量状态更新事件
 */
public class BatchStatusUpdateEvent {
    private Message.MessageStatus status;
    private List<String> messageIds;

    public Message.MessageStatus getStatus() {
        return status;
    }

    public void setStatus(Message.MessageStatus status) {
        this.status = status;
    }

    public List<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<String> messageIds) {
        this.messageIds = messageIds;
    }
} 