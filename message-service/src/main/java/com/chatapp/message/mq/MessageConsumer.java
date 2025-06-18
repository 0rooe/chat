package com.chatapp.message.mq;

import com.chatapp.message.model.Message;
import com.chatapp.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消息送达状态消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageConsumer {

    private final MessageService messageService;

    @RabbitListener(queues = "chat.message.delivery.queue")
    public void handleDeliveryEvent(MessageDeliveryEvent event) {
        log.info("接收到消息送达事件: {}", event);
        
        // 根据事件类型处理消息状态
        switch (event.getType()) {
            case DELIVERED:
                messageService.updateMessageStatus(event.getMessageId(), Message.MessageStatus.DELIVERED);
                break;
            case READ:
                messageService.updateMessageStatus(event.getMessageId(), Message.MessageStatus.READ);
                break;
            case FAILED:
                messageService.updateMessageStatus(event.getMessageId(), Message.MessageStatus.FAILED);
                break;
            default:
                log.warn("未知的送达事件类型: {}", event.getType());
        }
    }
} 