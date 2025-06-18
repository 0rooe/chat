package com.chatapp.notification.mq;

import com.chatapp.notification.dto.NotificationDTO;
import com.chatapp.notification.model.Notification;
import com.chatapp.notification.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageNotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "chat.notification.queue")
    public void handleNotification(String message) {
        try {
            // 解析消息
            MessageEvent messageEvent = objectMapper.readValue(message, MessageEvent.class);
            log.info("收到新消息事件: {}", messageEvent);
            
            // 创建通知
            NotificationDTO notificationDTO = createNotificationFromMessage(messageEvent);
            
            // 发送即时通知
            notificationService.sendInstantNotification(notificationDTO);
        } catch (JsonProcessingException e) {
            log.error("解析消息失败: {}", message, e);
        } catch (Exception e) {
            log.error("处理消息通知失败", e);
        }
    }
    
    private NotificationDTO createNotificationFromMessage(MessageEvent messageEvent) {
        String title;
        String content;
        
        if (messageEvent.getMessageType().equals("PRIVATE")) {
            title = "新的私聊消息";
            content = String.format("你收到来自 %s 的新消息", messageEvent.getSenderName());
        } else {
            title = String.format("群聊 %s 的新消息", messageEvent.getReceiverName());
            content = String.format("%s 在群里发送了新消息", messageEvent.getSenderName());
        }
        
        return NotificationDTO.builder()
                .userId(messageEvent.getReceiverId())
                .type(Notification.NotificationType.MESSAGE)
                .title(title)
                .content(content)
                .referenceId(messageEvent.getMessageId())
                .status(Notification.NotificationStatus.UNREAD)
                .build();
    }
    
    // 内部类：表示从消息服务接收的事件
    static class MessageEvent {
        private String messageId;
        private Long senderId;
        private String senderName;
        private Long receiverId;
        private String receiverName;
        private String messageType; // PRIVATE 或 GROUP
        private String content;
        
        // getter 和 setter
        public String getMessageId() {
            return messageId;
        }
        
        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
        
        public Long getSenderId() {
            return senderId;
        }
        
        public void setSenderId(Long senderId) {
            this.senderId = senderId;
        }
        
        public String getSenderName() {
            return senderName;
        }
        
        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }
        
        public Long getReceiverId() {
            return receiverId;
        }
        
        public void setReceiverId(Long receiverId) {
            this.receiverId = receiverId;
        }
        
        public String getReceiverName() {
            return receiverName;
        }
        
        public void setReceiverName(String receiverName) {
            this.receiverName = receiverName;
        }
        
        public String getMessageType() {
            return messageType;
        }
        
        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        @Override
        public String toString() {
            return "MessageEvent{" +
                    "messageId='" + messageId + '\'' +
                    ", senderId=" + senderId +
                    ", senderName='" + senderName + '\'' +
                    ", receiverId=" + receiverId +
                    ", receiverName='" + receiverName + '\'' +
                    ", messageType='" + messageType + '\'' +
                    ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 20)) + "..." : "null") + '\'' +
                    '}';
        }
    }
} 