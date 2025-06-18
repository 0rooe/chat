package com.chatapp.message.mq;

/**
 * 消息送达事件
 */
public class MessageDeliveryEvent {
    private String messageId;
    private EventType type;
    private Long userId;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "MessageDeliveryEvent{" +
                "messageId='" + messageId + '\'' +
                ", type=" + type +
                ", userId=" + userId +
                '}';
    }

    /**
     * 事件类型
     */
    public enum EventType {
        DELIVERED,  // 消息已送达
        READ,       // 消息已读
        FAILED      // 消息发送失败
    }
} 