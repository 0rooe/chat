package com.chatapp.message.service.impl;

import com.chatapp.message.dto.ConversationResponse;
import com.chatapp.message.dto.MessageDTO;
import com.chatapp.message.exception.ResourceNotFoundException;
import com.chatapp.message.exception.UnauthorizedException;
import com.chatapp.message.model.Conversation;
import com.chatapp.message.model.Message;
import com.chatapp.message.repository.ConversationRepository;
import com.chatapp.message.service.ConversationService;
import com.chatapp.message.service.MessageService;
import com.chatapp.message.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会话服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserService userService;
    private final MessageService messageService;

    @Override
    @Transactional
    public ConversationResponse createOrGetConversation(Long userId, Long recipientId) {
        // 检查用户是否存在
        Map<String, Object> userInfo = userService.getUserBasicInfo(userId);
        Map<String, Object> recipientInfo = userService.getUserBasicInfo(recipientId);
        
        if (userInfo == null || recipientInfo == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        
        // 查找现有会话
        Optional<Conversation> existingConversation = conversationRepository.findByUserIdAndFriendId(userId, recipientId);
        
        if (existingConversation.isPresent()) {
            // 如果已经存在会话，直接返回
            return convertToDto(existingConversation.get(), recipientInfo);
        }
        
        // 创建新会话
        String conversationId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        // 确保用户昵称不为空
        String recipientNickname = recipientInfo.containsKey("nickname") && recipientInfo.get("nickname") != null 
                ? recipientInfo.get("nickname").toString() 
                : "用户" + recipientId;
                
        String userNickname = userInfo.containsKey("nickname") && userInfo.get("nickname") != null 
                ? userInfo.get("nickname").toString() 
                : "用户" + userId;
                
        // 头像处理
        String recipientAvatar = recipientInfo.containsKey("avatar") && recipientInfo.get("avatar") != null 
                ? recipientInfo.get("avatar").toString() 
                : null;
                
        String userAvatar = userInfo.containsKey("avatar") && userInfo.get("avatar") != null 
                ? userInfo.get("avatar").toString() 
                : null;
        
        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .userId(userId)
                .friendId(recipientId)
                .title(recipientNickname)
                .avatar(recipientAvatar)
                .lastMessageTime(now)
                .createTime(now)
                .unread(false)
                .unreadCount(0)
                .build();
        
        // 发送系统欢迎消息
        String welcomeMessage = "你们已成为好友，现在可以开始聊天了！";
        
        MessageDTO systemMessage = MessageDTO.builder()
                .senderId(0L) // 系统消息发送者ID为0
                .receiverId(userId)
                .content(welcomeMessage)
                .contentType("TEXT")
                .messageType("SYSTEM")
                .status("DELIVERED")
                .createTime(now)
                .build();
                
        try {
            // 保存系统消息
            MessageDTO savedMessage = messageService.sendMessage(systemMessage);
            log.info("发送系统欢迎消息成功: {}", savedMessage);
            
            // 更新会话的最后消息
            conversation.setLastMessageContent(welcomeMessage);
            conversation.setLastMessageTime(now);
        } catch (Exception e) {
            log.error("发送系统欢迎消息失败: {}", e.getMessage(), e);
        }
        
        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("创建新会话: {}", savedConversation);
        
        // 创建对方的会话记录
        Conversation recipientConversation = Conversation.builder()
                .id(UUID.randomUUID().toString())
                .userId(recipientId)
                .friendId(userId)
                .title(userNickname)
                .avatar(userAvatar)
                .lastMessageTime(now)
                .lastMessageContent(welcomeMessage)
                .createTime(now)
                .unread(true)
                .unreadCount(1)
                .build();
        
        conversationRepository.save(recipientConversation);
        log.info("创建接收者会话: {}", recipientConversation);
        
        // 给接收者也发送一条相同的系统消息
        MessageDTO recipientSystemMessage = MessageDTO.builder()
                .senderId(0L)
                .receiverId(recipientId)
                .content(welcomeMessage)
                .contentType("TEXT")
                .messageType("SYSTEM")
                .status("DELIVERED")
                .createTime(now)
                .build();
                
        try {
            messageService.sendMessage(recipientSystemMessage);
        } catch (Exception e) {
            log.error("发送接收者系统欢迎消息失败: {}", e.getMessage(), e);
        }
        
        return convertToDto(savedConversation, recipientInfo);
    }

    @Override
    public List<ConversationResponse> getUserConversations(Long userId) {
        // 获取用户的所有会话
        List<Conversation> conversations = conversationRepository.findAllByUserIdOrderByLastMessageTimeDesc(userId);
        
        return conversations.stream()
                .map(conversation -> {
                    try {
                        Map<String, Object> friendInfo = userService.getUserBasicInfo(conversation.getFriendId());
                        return convertToDto(conversation, friendInfo);
                    } catch (Exception e) {
                        log.error("获取好友信息失败: {}", e.getMessage(), e);
                        // 如果获取好友信息失败，使用默认值
                        Map<String, Object> defaultFriendInfo = new HashMap<>();
                        defaultFriendInfo.put("id", conversation.getFriendId());
                        defaultFriendInfo.put("nickname", conversation.getTitle() != null ? conversation.getTitle() : "未知用户");
                        defaultFriendInfo.put("avatar", conversation.getAvatar());
                        return convertToDto(conversation, defaultFriendInfo);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public ConversationResponse getConversation(String conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("会话不存在"));
        
        // 检查权限
        if (!conversation.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权访问此会话");
        }
        
        // 获取好友信息
        Map<String, Object> friendInfo = userService.getUserBasicInfo(conversation.getFriendId());
        
        return convertToDto(conversation, friendInfo);
    }

    @Override
    @Transactional
    public void deleteConversation(String conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("会话不存在"));
        
        // 检查权限
        if (!conversation.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权删除此会话");
        }
        
        // 删除会话
        conversationRepository.delete(conversation);
        log.info("删除会话: {}", conversationId);
    }
    
    /**
     * 将会话实体转换为DTO
     */
    private ConversationResponse convertToDto(Conversation conversation, Map<String, Object> friendInfo) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .userId(conversation.getUserId())
                .friendId(conversation.getFriendId())
                .title(conversation.getTitle())
                .avatar(conversation.getAvatar())
                .lastMessageTime(conversation.getLastMessageTime())
                .lastMessageContent(conversation.getLastMessageContent())
                .unread(conversation.getUnread())
                .unreadCount(conversation.getUnreadCount())
                .createTime(conversation.getCreateTime())
                .friendInfo(friendInfo)
                .build();
    }
} 