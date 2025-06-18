package com.chatapp.message.controller;

import com.chatapp.message.config.WebSocketUserRegistry;
import com.chatapp.message.dto.EncryptedMessageDto;
import com.chatapp.message.dto.MessageDTO;
import com.chatapp.message.service.EncryptionService;
import com.chatapp.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Set;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final EncryptionService encryptionService;
    private final WebSocketUserRegistry userRegistry;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageDTO messageDto) {
        log.info("收到WebSocket消息: 发送者={}, 接收者={}, 内容={}", 
                messageDto.getSenderId(), messageDto.getReceiverId(), messageDto.getContent());
        
        try {
            // 更新发送者的活跃时间
            String senderId = String.valueOf(messageDto.getSenderId());
            userRegistry.updateUserLastActive(senderId);
            
            // 保存消息
            MessageDTO savedMessage = messageService.saveMessage(messageDto);
            log.info("消息保存成功，准备发送WebSocket消息");
            
            // 根据消息类型处理不同的发送逻辑
            if ("GROUP".equals(messageDto.getMessageType())) {
                // 群组消息：发送到群组话题，由订阅者接收
                messagingTemplate.convertAndSend(
                        "/topic/group/" + messageDto.getReceiverId(),
                        savedMessage);
                log.info("群组消息已发送到话题: /topic/group/{}", messageDto.getReceiverId());
            } else {
                // 私聊消息：发送到发送者和接收者的会话队列
                String receiverId = String.valueOf(messageDto.getReceiverId());
                
                // 发送到接收者的所有会话
                Set<String> receiverSessions = userRegistry.getUserSessions(receiverId);
                log.info("获取到接收者会话数量: userId={}, 会话数={}", receiverId, receiverSessions.size());
                
                for (String sessionId : receiverSessions) {
                    try {
                        messagingTemplate.convertAndSend("/queue/messages-" + sessionId, savedMessage);
                        log.info("消息已发送到接收者会话: userId={}, sessionId={}", receiverId, sessionId);
                    } catch (Exception e) {
                        log.error("发送消息到接收者会话失败: userId={}, sessionId={}", receiverId, sessionId, e);
                    }
                }
                
                // 如果发送者和接收者不是同一人，也发送给发送者（确保发送者能看到自己发的消息）
                if (!senderId.equals(receiverId)) {
                    Set<String> senderSessions = userRegistry.getUserSessions(senderId);
                    log.info("获取到发送者会话数量: userId={}, 会话数={}", senderId, senderSessions.size());
                    
                    for (String sessionId : senderSessions) {
                        try {
                            messagingTemplate.convertAndSend("/queue/messages-" + sessionId, savedMessage);
                            log.info("消息已发送到发送者会话: userId={}, sessionId={}", senderId, sessionId);
                        } catch (Exception e) {
                            log.error("发送消息到发送者会话失败: userId={}, sessionId={}", senderId, sessionId, e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("发送消息时发生错误", e);
            // 可以考虑发送错误消息回客户端
        }
    }
    
    @MessageMapping("/chat.encrypted")
    public void sendEncryptedMessage(@Payload EncryptedMessageDto encryptedMessageDto) {
        log.info("收到加密WebSocket消息: 发送者={}, 接收者={}", 
                encryptedMessageDto.getSenderId(), encryptedMessageDto.getReceiverId());
        
        // 保存加密消息
        MessageDTO savedMessage = messageService.saveEncryptedMessage(encryptedMessageDto);
        
        // 发送到接收者的私人队列
        messagingTemplate.convertAndSendToUser(
                String.valueOf(encryptedMessageDto.getReceiverId()),
                "/queue/messages",
                savedMessage);
    }
    
    @MessageMapping("/chat.sendGroupAnnouncement")
    public void sendGroupAnnouncement(@Payload MessageDTO messageDto) {
        log.info("收到群公告消息: 发送者={}, 群组={}, 类型={}", 
                messageDto.getSenderId(), messageDto.getReceiverId(), messageDto.getMessageType());
        
        try {
            // 直接广播群公告，不保存为常规消息
            messagingTemplate.convertAndSend(
                    "/topic/group/" + messageDto.getReceiverId(),
                    messageDto);
            log.info("群公告已广播到话题: /topic/group/{}", messageDto.getReceiverId());
        } catch (Exception e) {
            log.error("广播群公告时发生错误", e);
        }
    }

    @MessageMapping("/heartbeat")
    public void handleHeartbeat(@Payload Map<String, Object> heartbeat, 
                               @Header("simpSessionId") String sessionId) {
        String userId = (String) heartbeat.get("userId");
        if (userId != null) {
            userRegistry.updateUserLastActive(userId);
            log.debug("收到心跳: userId={}, sessionId={}", userId, sessionId);
        }
    }
} 