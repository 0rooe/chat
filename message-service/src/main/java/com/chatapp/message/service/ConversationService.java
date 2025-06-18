package com.chatapp.message.service;

import com.chatapp.message.dto.ConversationResponse;

import java.util.List;

/**
 * 会话服务接口
 */
public interface ConversationService {
    
    /**
     * 创建或获取会话
     * 
     * @param userId 当前用户ID
     * @param recipientId 接收者/好友ID
     * @return 会话响应对象
     */
    ConversationResponse createOrGetConversation(Long userId, Long recipientId);
    
    /**
     * 获取用户的所有会话
     * 
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ConversationResponse> getUserConversations(Long userId);
    
    /**
     * 获取特定会话
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话响应对象
     */
    ConversationResponse getConversation(String conversationId, Long userId);
    
    /**
     * 删除会话
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     */
    void deleteConversation(String conversationId, Long userId);
} 