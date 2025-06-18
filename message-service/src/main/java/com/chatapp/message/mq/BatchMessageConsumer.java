package com.chatapp.message.mq;

import com.chatapp.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 批量消息状态更新消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BatchMessageConsumer {

    private final MessageService messageService;

    @RabbitListener(queues = "chat.batch.update.queue")
    public void handleBatchUpdate(BatchStatusUpdateEvent event) {
        log.info("接收到批量消息状态更新事件，状态: {}, 消息数: {}", event.getStatus(), event.getMessageIds().size());
        messageService.updateMessageStatusBatch(event.getMessageIds(), event.getStatus());
    }
} 