package com.chatapp.message.controller;

import com.chatapp.message.dto.MessageDTO;
import com.chatapp.message.dto.MessageQueryParams;
import com.chatapp.message.dto.MessageRequest;
import com.chatapp.message.dto.MessageStatusUpdateRequest;
import com.chatapp.message.dto.EncryptedMessageDto;
import com.chatapp.message.dto.MarkAsReadRequest;
import com.chatapp.message.exception.UnauthorizedException;
import com.chatapp.message.model.Message;
import com.chatapp.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送消息
     */
    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(@RequestHeader("X-User-ID") Long userId, 
                                                 @Valid @RequestBody MessageRequest request) {
        log.info("用户 {} 发送消息给接收者 {}, 类型: {}", userId, request.getReceiverId(), request.getMessageType());
        
        // 转换请求DTO为消息DTO
        MessageDTO messageDto = MessageDTO.builder()
                .senderId(userId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .contentType(request.getContentType().toString())
                .messageType(request.getMessageType().toString())
                .status(Message.MessageStatus.SENDING.toString())
                .createTime(LocalDateTime.now())
                .attachments(request.getAttachments())
                .build();
                
        // 发送消息
        MessageDTO sentMessage = messageService.sendMessage(messageDto);
        return new ResponseEntity<>(sentMessage, HttpStatus.CREATED);
    }
    
    /**
     * 获取消息详情
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageDTO> getMessageById(@PathVariable String messageId) {
        log.info("获取消息详情: {}", messageId);
        
        MessageDTO message = messageService.getMessageById(messageId);
        return ResponseEntity.ok(message);
    }
    
    /**
     * 更新消息状态
     */
    @PutMapping("/{messageId}/status")
    public ResponseEntity<MessageDTO> updateMessageStatus(@PathVariable String messageId,
                                                       @Valid @RequestBody MessageStatusUpdateRequest request) {
        log.info("更新消息 {} 状态为: {}", messageId, request.getStatus());
        
        MessageDTO updatedMessage = messageService.updateMessageStatus(messageId, request.getStatus());
        return ResponseEntity.ok(updatedMessage);
    }
    
    /**
     * 批量更新消息状态
     */
    @PutMapping("/status/batch")
    public ResponseEntity<Integer> updateMessageStatusBatch(@RequestBody List<String> messageIds,
                                                           @RequestParam Message.MessageStatus status) {
        log.info("批量更新 {} 条消息状态为: {}", messageIds.size(), status);
        
        int updatedCount = messageService.updateMessageStatusBatch(messageIds, status);
        return ResponseEntity.ok(updatedCount);
    }
    
    /**
     * 获取私聊历史记录（分页）
     */
    @GetMapping("/private")
    public ResponseEntity<Page<MessageDTO>> getPrivateMessages(@RequestParam Long userId1,
                                                              @RequestParam Long userId2,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        log.info("查询用户 {} 和用户 {} 的私聊历史, 页码: {}, 大小: {}", userId1, userId2, page, size);
        
        Page<MessageDTO> messages = messageService.getPrivateMessages(userId1, userId2, page, size);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * 获取私聊历史记录（列表形式，用于前端）
     */
    @GetMapping("/private/list")
    public ResponseEntity<List<MessageDTO>> getPrivateMessagesList(@RequestParam("userId") Long userId,
                                                                   @RequestParam("friendId") Long friendId,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "50") int size) {
        log.info("查询用户 {} 和用户 {} 的私聊历史列表, 页码: {}, 大小: {}", userId, friendId, page, size);
        
        try {
            Page<MessageDTO> messagesPage = messageService.getPrivateMessages(userId, friendId, page, size);
            // 返回消息内容，按时间正序排列（最早的在前面）
            List<MessageDTO> messages = new ArrayList<>(messagesPage.getContent());
            messages.sort((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()));
            
            log.info("成功获取用户 {} 和用户 {} 的 {} 条私聊历史消息", userId, friendId, messages.size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("获取用户 {} 和用户 {} 的私聊历史失败: {}", userId, friendId, e.getMessage(), e);
            // 返回空列表而不是抛出异常
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    /**
     * 获取群聊历史记录（分页）
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Page<MessageDTO>> getGroupMessages(@PathVariable Long groupId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        log.info("查询群组 {} 的消息历史, 页码: {}, 大小: {}", groupId, page, size);
        
        Page<MessageDTO> messages = messageService.getGroupMessages(groupId, page, size);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * 获取群聊历史记录（列表形式，用于前端）
     */
    @GetMapping("/group/{groupId}/list")
    public ResponseEntity<List<MessageDTO>> getGroupMessagesList(@PathVariable Long groupId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "50") int size) {
        log.info("查询群组 {} 的消息历史列表, 页码: {}, 大小: {}", groupId, page, size);
        
        Page<MessageDTO> messagesPage = messageService.getGroupMessages(groupId, page, size);
        // 返回消息内容，按时间正序排列（最早的在前面）
        List<MessageDTO> messages = new ArrayList<>(messagesPage.getContent());
        messages.sort((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()));
        
        return ResponseEntity.ok(messages);
    }
    
    /**
     * 复杂查询消息
     */
    @GetMapping("/search")
    public ResponseEntity<List<MessageDTO>> searchMessages(
            @RequestHeader("X-User-ID") Long userId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("用户 {} 搜索消息，关键词: {}", userId, keyword);
        List<MessageDTO> messages = messageService.searchMessages(keyword, userId, page, size);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * 获取用户未读消息
     */
    @GetMapping("/unread/{userId}")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages(@PathVariable Long userId) {
        log.info("获取用户 {} 的未读消息", userId);
        
        List<MessageDTO> unreadMessages = messageService.getUnreadMessages(userId);
        return ResponseEntity.ok(unreadMessages);
    }
    
    /**
     * 标记消息为已读
     */
    @PostMapping("/mark-read")
    public ResponseEntity<Integer> markMessagesAsRead(@RequestBody MarkAsReadRequest request) {
        log.info("标记用户 {} 来自发送者 {} 的消息为已读", request.getReceiverId(), request.getSenderId());
        
        int updatedCount = messageService.markMessagesAsRead(request.getReceiverId(), request.getSenderId());
        return ResponseEntity.ok(updatedCount);
    }
    
    /**
     * 标记所有未读消息为已读
     */
    @PostMapping("/mark-all-read/{userId}")
    public ResponseEntity<Integer> markAllMessagesAsRead(@PathVariable Long userId) {
        log.info("标记用户 {} 的所有消息为已读", userId);
        
        int updatedCount = messageService.markAllMessagesAsRead(userId);
        return ResponseEntity.ok(updatedCount);
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        log.info("删除消息: {}", messageId);
        
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 撤回消息
     */
    @PutMapping("/{messageId}/recall")
    public ResponseEntity<MessageDTO> recallMessage(@RequestHeader("X-User-ID") Long userId,
                                                  @PathVariable String messageId) {
        log.info("用户 {} 撤回消息: {}", userId, messageId);
        
        MessageDTO recalledMessage = messageService.recallMessage(messageId, userId);
        return ResponseEntity.ok(recalledMessage);
    }

    @PostMapping("/encrypted")
    public ResponseEntity<MessageDTO> sendEncryptedMessage(
            @RequestHeader("X-User-ID") Long userId,
            @Valid @RequestBody EncryptedMessageDto encryptedMessageDto) {
        log.info("用户 {} 发送加密消息到用户 {}", userId, encryptedMessageDto.getReceiverId());
        
        if (!userId.equals(encryptedMessageDto.getSenderId())) {
            throw new UnauthorizedException("无权以其他用户身份发送消息");
        }
        
        MessageDTO message = messageService.saveEncryptedMessage(encryptedMessageDto);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }
} 