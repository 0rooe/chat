package com.chatapp.message.service;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 获取用户基本信息
     * 
     * @param userId 用户ID
     * @return 用户基本信息
     */
    Map<String, Object> getUserBasicInfo(Long userId);
} 