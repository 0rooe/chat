package com.chatapp.message.service;

import java.util.List;
import java.util.Map;

/**
 * 聊天服务接口
 */
public interface ChatService {
    
    /**
     * 获取用户最近的聊天列表
     * 
     * @param userId 用户ID
     * @return 聊天列表
     */
    List<Map<String, Object>> getRecentChats(Long userId);
} 