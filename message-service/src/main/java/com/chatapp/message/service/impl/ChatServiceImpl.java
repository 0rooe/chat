package com.chatapp.message.service.impl;

import com.chatapp.message.model.Message;
import com.chatapp.message.repository.MessageRepository;
import com.chatapp.message.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final MessageRepository messageRepository;

    @Override
    public List<Map<String, Object>> getRecentChats(Long userId) {
        log.info("获取用户{}的最近聊天列表", userId);
        
        // 获取用户相关的私聊消息
        List<Message> privateMessages = messageRepository.findRecentPrivateMessagesByUserId(userId);
        
        // 获取用户参与的群聊消息
        List<Message> groupMessages = messageRepository.findRecentGroupMessagesByUserId(userId);
        
        // 合并并处理聊天列表
        return processChatList(userId, privateMessages, groupMessages);
    }
    
    private List<Map<String, Object>> processChatList(Long userId, List<Message> privateMessages, List<Message> groupMessages) {
        // 存储每个聊天的最后一条消息
        Map<String, Message> lastMessages = new HashMap<>();
        
        // 处理私聊消息
        for (Message message : privateMessages) {
            // 确定聊天对象
            Long chatId = message.getSenderId().equals(userId) ? message.getReceiverId() : message.getSenderId();
            String chatKey = "private_" + chatId;
            
            // 如果尚未记录该聊天的最后消息，或者当前消息比已记录的更新
            if (!lastMessages.containsKey(chatKey) || 
                message.getCreateTime().isAfter(lastMessages.get(chatKey).getCreateTime())) {
                lastMessages.put(chatKey, message);
            }
        }
        
        // 处理群聊消息
        for (Message message : groupMessages) {
            String chatKey = "group_" + message.getReceiverId();
            
            if (!lastMessages.containsKey(chatKey) || 
                message.getCreateTime().isAfter(lastMessages.get(chatKey).getCreateTime())) {
                lastMessages.put(chatKey, message);
            }
        }
        
        // 转换为前端需要的格式并按最后消息时间排序
        return lastMessages.entrySet().stream()
            .map(entry -> {
                Map<String, Object> chatInfo = new HashMap<>();
                Message lastMessage = entry.getValue();
                String[] chatParts = entry.getKey().split("_");
                String chatType = chatParts[0];
                Long chatId = Long.valueOf(chatParts[1]);
                
                chatInfo.put("id", chatId);
                chatInfo.put("type", chatType);
                
                // 这里应该从用户服务获取名称，暂时用占位符
                chatInfo.put("name", chatType.equals("private") ? "用户" + chatId : "群组" + chatId);
                chatInfo.put("lastMessage", lastMessage.getContent());
                chatInfo.put("lastMessageTime", lastMessage.getCreateTime().toInstant(ZoneOffset.UTC).toEpochMilli());
                chatInfo.put("status", "ONLINE"); // 应该从用户服务获取状态
                
                if (chatType.equals("group")) {
                    chatInfo.put("memberCount", 3); // 应从群组服务获取成员数量
                }
                
                return chatInfo;
            })
            .sorted((a, b) -> {
                Long timeA = (Long) a.get("lastMessageTime");
                Long timeB = (Long) b.get("lastMessageTime");
                return timeB.compareTo(timeA); // 降序排序
            })
            .collect(Collectors.toList());
    }
} 